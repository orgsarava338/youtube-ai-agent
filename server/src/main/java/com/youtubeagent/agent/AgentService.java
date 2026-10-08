package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMToolCall;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentService {

    private static final int MAX_ITERATIONS = 5;

    private final ToolRegistry toolRegistry;
    private final DecisionModel decisionModel;
    private final ResponseModel responseModel;
    private final ObjectMapper objectMapper;

    public AgentService(
            ToolRegistry toolRegistry,
            DecisionModel decisionModel,
            ResponseModel responseModel,
            ObjectMapper objectMapper) {

        this.toolRegistry = toolRegistry;
        this.decisionModel = decisionModel;
        this.responseModel = responseModel;
        this.objectMapper = objectMapper;
    }

    public AgentResponse chat(String userMessage) {
        AgentContext context = new AgentContext(userMessage);

        while (context.iteration() < MAX_ITERATIONS) {
            context = context.nextIteration();
            log.info("Agent iteration: {}", context.iteration());

            LLMRequest request = buildRequest(context);
            AgentDecision decision = decisionModel.decide(request);

            if (decision.type() == AgentDecision.Type.FINAL_RESPONSE) {
                String finalResponse = responseModel.generate(buildResponseRequest(context));
                return new AgentResponse("final_answer", List.of(), finalResponse);
            }

            if (decision.type() == AgentDecision.Type.TOOL_CALLS) {
                context = context.addMessage(buildAssistantToolCallMessage(decision));
                
                for (ToolCall toolCall : decision.toolCalls()) {
                    log.info("Model called the tool: {}{}", toolCall.tool(), toolCall.arguments());
                    ToolExecutionResult toolResult = executeTool(toolCall);
                    context = context.addMessage(LLMMessage.toolResult(toolCall.id(), buildToolResultMessage(toolResult)));
                }

                continue;
            }

            throw new IllegalStateException("Unknown agent decision type: " + decision.type());
        }

        throw new IllegalStateException("Agent exceeded maximum iterations");
    }

    private LLMMessage buildAssistantToolCallMessage(AgentDecision decision) {
        List<LLMToolCall> toolCalls = decision.toolCalls()
            .stream()
            .map(toolCall -> new LLMToolCall(toolCall.id(), toolCall.tool(), toolCall.arguments()))
            .toList();

        return LLMMessage.assistantToolCalls(toolCalls);
    }

    private LLMRequest buildRequest(AgentContext context) {
        return new LLMRequest(
                null,
                List.copyOf(context.messages()),
                toolRegistry.getLLMToolDefinitions());
    }

    private LLMRequest buildResponseRequest(AgentContext context) {
        return new LLMRequest(null, List.copyOf(context.messages()), List.of());
    }

    private String buildToolResultMessage(ToolExecutionResult toolResult) {

        try {
            return objectMapper.writeValueAsString(
                    Map.of(
                            "type", "tool_result",
                            "callId", toolResult.callId(),
                                    "tool", toolResult.tool(),
                            "success", toolResult.success(),
                            "result", toolResult.result(),
                            "instructions",
                            "Treat this result as authoritative. "
                                    + "Use its actual values for subsequent tool calls. "
                                    + "Do not invent or guess values."));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build tool result message", e);
        }
    }

    private ToolExecutionResult executeTool(ToolCall toolCall) {
        try {
            String result = toolRegistry.execute(toolCall.tool(), toolCall.arguments());
            return ToolExecutionResult.success(toolCall.id(), toolCall.tool(), result);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolCall.tool(), e);
            return ToolExecutionResult.failure(
                    toolCall.id(),
                    toolCall.tool(),
                    "Tool execution failed: " + e.getMessage());
        }
    }
}