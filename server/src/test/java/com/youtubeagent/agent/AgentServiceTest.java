package com.youtubeagent.agent;

import com.youtubeagent.agent.tools.RepeatedToolCallGuard;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock private ToolRegistry toolRegistry;
    @Mock private DecisionModel decisionModel;
    @Mock private ResponseModel responseModel;
    @Mock private ObjectMapper objectMapper;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        RepeatedToolCallGuard repeatedToolCallGuard = new RepeatedToolCallGuard(new ObjectMapper());
        agentService = new AgentService(toolRegistry, decisionModel, responseModel, repeatedToolCallGuard,
                objectMapper);
        when(toolRegistry.getLLMToolDefinitions()).thenReturn(List.of());
    }

    @Test
    void returnsFinalAnswerWhenDecisionIsDone() {
        List<LLMMessage> history = List.of(
                new LLMMessage("user", "previous question"),
                new LLMMessage("user", "current question"));
        when(decisionModel.decide(any(LLMRequest.class))).thenReturn(AgentDecision.done());
        when(responseModel.generate(any(LLMRequest.class))).thenReturn("Here is the answer.");

        AgentTurnResult result = agentService.runTurn("user-1", "current question", history);

        assertEquals("final_answer", result.response().type());
        assertEquals("Here is the answer.", result.response().content());
        assertEquals(1, result.messages().size());
        assertEquals("assistant", result.messages().getFirst().role());
        assertEquals("Here is the answer.", result.messages().getFirst().content());

        ArgumentCaptor<LLMRequest> requestCaptor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(responseModel).generate(requestCaptor.capture());
        assertFalse(requestCaptor.getValue().hasTools());
        assertTrue(requestCaptor.getValue().messages().stream()
                .anyMatch(message -> "previous question".equals(message.content())));
        assertTrue(requestCaptor.getValue().messages().stream()
                .anyMatch(message -> "current question".equals(message.content())));
    }

    @Test
    void executesToolAndPreservesToolCallAndResultInHistory() {
        ToolCall call = new ToolCall("call-1", "echo", Map.of("text", "hello"));
        when(toolRegistry.hasTool("echo")).thenReturn(true);
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of(call)))
                .thenReturn(AgentDecision.done());
        when(toolRegistry.execute(any(ToolExecutionContext.class), eq("echo"), eq(call.arguments())))
                .thenReturn("hello");
        when(responseModel.generate(any(LLMRequest.class))).thenReturn("The tool returned hello.");

        AgentTurnResult result = agentService.runTurn("user-1", "say hello", List.of());

        assertEquals("The tool returned hello.", result.response().content());
        assertTrue(result.messages().stream().anyMatch(LLMMessage::hasToolCalls));
        assertTrue(result.messages().stream().anyMatch(message ->
                "tool".equals(message.role()) && "call-1".equals(message.toolCallId())));
        verify(toolRegistry).execute(any(ToolExecutionContext.class), eq("echo"), eq(call.arguments()));
        verify(decisionModel, times(2)).decide(any(LLMRequest.class));
    }

    @Test
    void reportsToolFailureToTheNextDecisionAndStillProducesFinalAnswer() throws Exception {
        ToolCall call = new ToolCall("call-fail", "echo", Map.of());
        when(toolRegistry.hasTool("echo")).thenReturn(true);
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of(call)))
                .thenReturn(AgentDecision.done());
        when(toolRegistry.execute(any(ToolExecutionContext.class), eq("echo"), any()))
                .thenThrow(new IllegalStateException("service unavailable"));
        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation ->
                invocation.getArgument(0).toString());
        when(responseModel.generate(any(LLMRequest.class))).thenReturn("The operation could not be completed.");

        AgentTurnResult result = agentService.runTurn("user-1", "try the tool", List.of());

        assertEquals("The operation could not be completed.", result.response().content());
        LLMMessage toolResult = result.messages().stream()
                .filter(message -> "tool".equals(message.role()))
                .findFirst()
                .orElseThrow();
        assertTrue(toolResult.content().contains("success=false"));
        assertTrue(toolResult.content().contains("Tool execution failed: service unavailable"));
    }

    @Test
    void rejectsDuplicateToolCallIdsBeforeExecutingAnyTool() {
        ToolCall first = new ToolCall("duplicate", "echo", Map.of());
        ToolCall second = new ToolCall("duplicate", "echo", Map.of());
        when(toolRegistry.hasTool("echo")).thenReturn(true);
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of(first, second)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> agentService.runTurn("user-1", "call twice", List.of()));

        assertTrue(exception.getMessage().contains("Duplicate tool call ID"));
        verify(toolRegistry, never()).execute(any(), any(), any());
    }

    @Test
    void rejectsUnknownToolCallsBeforeExecution() {
        ToolCall call = new ToolCall("call-1", "unknown", Map.of());
        when(toolRegistry.hasTool("unknown")).thenReturn(false);
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of(call)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> agentService.runTurn("user-1", "use unknown tool", List.of()));

        assertTrue(exception.getMessage().contains("unknown tool"));
        verify(toolRegistry, never()).execute(any(), any(), any());
    }

    @Test
    void returnsBestAvailableAnswerAfterIterationLimit() {
        ToolCall call = new ToolCall("call-1", "echo", Map.of());
        when(toolRegistry.hasTool("echo")).thenReturn(true);
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of(call)));
        when(toolRegistry.execute(any(ToolExecutionContext.class), eq("echo"), any()))
                .thenReturn("ok");
        when(responseModel.generate(any(LLMRequest.class))).thenReturn("Partial answer.");

        AgentTurnResult result = agentService.runTurn("user-1", "keep trying", List.of());

        assertEquals("Partial answer.", result.response().content());
        verify(decisionModel, times(5)).decide(any(LLMRequest.class));
        ArgumentCaptor<LLMRequest> requestCaptor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(responseModel).generate(requestCaptor.capture());
        assertEquals("system", requestCaptor.getValue().messages().getFirst().role());
        assertTrue(requestCaptor.getValue().messages().getFirst().content().contains("maximum tool-execution iterations"));
    }
}
