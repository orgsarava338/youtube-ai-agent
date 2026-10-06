package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.routing.LLMRouter;
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

        LLMRouter router = mock(LLMRouter.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("No tools available.");

        when(router.generate(any(), any()))
                .thenReturn(new LLMResponse(
                        """
                                {
                                  "type": "final_answer",
                                  "content": "Hello"
                                }
                                """,
                        "openrouter",
                        "test-model"));

        AgentService service = new AgentService(
                router,
                registry,
                new ObjectMapper());

        AgentResponse response = service.chat("Hello");

        assertEquals(
                "final_answer",
                response.type());

        assertEquals(
                "Hello",
                response.content());

        verify(router, times(1))
                .generate(any(), any());
    }

    @Test
    void shouldExecuteToolAndPreserveConversationHistory() {

        LLMRouter router = mock(LLMRouter.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("calculate | Calculator");

        when(registry.execute(
                eq("calculate"),
                eq(Map.of("expression", "5 - 2"))))
                .thenReturn("3.0");

        when(router.generate(any(), any()))
                .thenReturn(
                        new LLMResponse(
                                """
                                        {
                                          "type": "tool_calls",
                                          "calls": [
                                            {
                                              "tool": "calculate",
                                              "arguments": {
                                                "expression": "5 - 2"
                                              }
                                            }
                                          ]
                                        }
                                        """,
                                "openrouter",
                                "model-a"),
                        new LLMResponse(
                                """
                                        {
                                          "type": "final_answer",
                                          "content": "The answer is 3."
                                        }
                                        """,
                                "openrouter",
                                "model-a"));

        AgentService service = new AgentService(
                router,
                registry,
                new ObjectMapper());

        AgentResponse response = service.chat("What is 5 - 2?");

        assertEquals(
                "final_answer",
                response.type());

        assertEquals(
                "The answer is 3.",
                response.content());

        verify(router, times(2))
                .generate(any(), any());

        verify(registry)
                .execute(
                        "calculate",
                        Map.of("expression", "5 - 2"));
    }

    @Test
    void shouldRejectInvalidLlmResponse() {

        LLMRouter router = mock(LLMRouter.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("No tools available.");

        when(router.generate(any(), any()))
                .thenReturn(new LLMResponse(
                        "not valid json",
                        "openrouter",
                        "test-model"));

        AgentService service = new AgentService(
                router,
                registry,
                new ObjectMapper());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.chat("hello"));

        assertEquals(
                "Invalid LLM response",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnknownAgentResponseType() {

        LLMRouter router = mock(LLMRouter.class);
        ToolRegistry registry = mock(ToolRegistry.class);

        when(registry.getToolDescriptions())
                .thenReturn("No tools available.");

        when(router.generate(any(), any()))
                .thenReturn(new LLMResponse(
                        """
                                {
                                  "type": "something_else",
                                  "content": "test"
                                }
                                """,
                        "openrouter",
                        "test-model"));

        AgentService service = new AgentService(
                router,
                registry,
                new ObjectMapper());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.chat("hello"));

        assertTrue(
                exception.getMessage()
                        .contains("Unknown agent response type"));
    }
}