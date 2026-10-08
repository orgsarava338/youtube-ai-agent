package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgentServiceTest {

    @Test
    void shouldReturnFinalAnswerImmediately() {
        DecisionModel decisionModel = mock(DecisionModel.class);
        ResponseModel responseModel = mock(ResponseModel.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("No tools available.");
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.finalResponse("Hello"));
        when(responseModel.generate(any(LLMRequest.class)))
                .thenReturn("Hello");

        AgentService service = new AgentService(
                registry,
                decisionModel,
                responseModel,
                new ObjectMapper());

        AgentResponse response = service.chat("Hello");

        assertEquals("final_answer", response.type());
        assertEquals("Hello", response.content());
        verify(decisionModel).decide(any(LLMRequest.class));
        verify(responseModel).generate(any(LLMRequest.class));
    }

    @Test
    void shouldExecuteToolAndPreserveConversationHistory() {
        DecisionModel decisionModel = mock(DecisionModel.class);
        ResponseModel responseModel = mock(ResponseModel.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("calculate | Calculator");
        when(registry.execute(
                "calculate",
                Map.of("expression", "5 - 2")))
                .thenReturn("3.0");
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(
                        AgentDecision.toolCalls(List.of(
                                new ToolCall(
                                        "call-1",
                                        "calculate",
                                        Map.of("expression", "5 - 2")))),
                        AgentDecision.finalResponse("The answer is 3."));
        when(responseModel.generate(any(LLMRequest.class)))
                .thenReturn("The answer is 3.");

        AgentService service = new AgentService(
                registry,
                decisionModel,
                responseModel,
                new ObjectMapper());

        AgentResponse response = service.chat("What is 5 - 2?");

        assertEquals("final_answer", response.type());
        assertEquals("The answer is 3.", response.content());

        var requests = org.mockito.ArgumentCaptor.forClass(LLMRequest.class);
        verify(decisionModel, times(2)).decide(requests.capture());
        assertTrue(requests.getAllValues().get(1).messages().stream()
                .anyMatch(message -> "tool".equals(message.role())
                        && "call-1".equals(message.toolCallId())
                        && message.content() != null
                        && message.content().contains("\"result\":\"3.0\"")));
        verify(registry).execute(
                "calculate",
                Map.of("expression", "5 - 2"));
    }

    @Test
    void shouldIncludeToolExecutionFailureInConversation() {
        DecisionModel decisionModel = mock(DecisionModel.class);
        ResponseModel responseModel = mock(ResponseModel.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("calculate | Calculator");
        when(registry.execute("calculate", Map.of()))
                .thenThrow(new IllegalStateException("calculation failed"));
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(
                        AgentDecision.toolCalls(List.of(
                                new ToolCall("call-1", "calculate", Map.of()))),
                        AgentDecision.finalResponse("The tool failed."));
        when(responseModel.generate(any(LLMRequest.class)))
                .thenReturn("The tool failed.");

        AgentService service = new AgentService(
                registry,
                decisionModel,
                responseModel,
                new ObjectMapper());

        AgentResponse response = service.chat("Calculate something.");

        assertEquals("The tool failed.", response.content());
        var requests = org.mockito.ArgumentCaptor.forClass(LLMRequest.class);
        verify(decisionModel, times(2)).decide(requests.capture());
        assertTrue(requests.getAllValues().get(1).messages().stream()
                .anyMatch(message -> "tool".equals(message.role())
                        && "call-1".equals(message.toolCallId())
                        && message.content() != null
                        && message.content().contains("Tool execution failed: calculation failed")));
    }

    @Test
    void shouldFailAfterMaximumDecisionIterations() {
        DecisionModel decisionModel = mock(DecisionModel.class);
        ResponseModel responseModel = mock(ResponseModel.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("No tools available.");
        when(decisionModel.decide(any(LLMRequest.class)))
                .thenReturn(AgentDecision.toolCalls(List.of()));

        AgentService service = new AgentService(
                registry,
                decisionModel,
                responseModel,
                new ObjectMapper());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.chat("Keep going."));

        assertEquals("Agent exceeded maximum iterations", exception.getMessage());
        verify(decisionModel, times(5)).decide(any(LLMRequest.class));
    }
}
