package com.youtubeagent.agent;

import com.youtubeagent.agent.tools.RepeatedToolCallGuard;
import com.youtubeagent.agent.tools.RepeatedToolCallGuard.TurnTracker;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMToolCall;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AgentService {


    private static final int MAX_ITERATIONS = 5;

    private final ToolRegistry toolRegistry;
    private final DecisionModel decisionModel;
    private final ResponseModel responseModel;
    private final RepeatedToolCallGuard repeatedToolCallGuard;
    private final ObjectMapper objectMapper;

    public AgentService(
            ToolRegistry toolRegistry,
            DecisionModel decisionModel,
            ResponseModel responseModel,
            RepeatedToolCallGuard repeatedToolCallGuard,
                    ObjectMapper objectMapper) {

        this.toolRegistry = toolRegistry;
        this.decisionModel = decisionModel;
        this.responseModel = responseModel;
        this.repeatedToolCallGuard = repeatedToolCallGuard;
        this.objectMapper = objectMapper;
    }

    public AgentTurnResult runTurn(String userId, String userMessage, List<LLMMessage> history) {

        TurnTracker toolCallTracker = repeatedToolCallGuard.newTurnTracker();

        ToolExecutionContext executionContext = new ToolExecutionContext(userId);
        int initialMessageCount = history.size();

        AgentContext context = new AgentContext(userMessage, history, 0);


        while (context.iteration() < MAX_ITERATIONS) {
            context = context.nextIteration();
            log.info("Agent iteration: {}", context.iteration());

            LLMRequest request = buildRequest(context);
            AgentDecision decision = decisionModel.decide(request);

            if (decision.type() == AgentDecision.Type.DONE) {
                String finalResponse = responseModel.generate(buildResponseRequest(context));
                context = context.addMessage(new LLMMessage("assistant", finalResponse));

                List<LLMMessage> generatedMessages = new ArrayList<>(
                        context.messages().subList(initialMessageCount, context.messages().size()));

                AgentResponse response = new AgentResponse("final_answer", List.of(), finalResponse);
                return new AgentTurnResult(response, generatedMessages);
            }

            if (decision.type() == AgentDecision.Type.TOOL_CALLS) {
                validateToolCalls(decision.toolCalls());
                context = context.addMessage(buildAssistantToolCallMessage(decision));

                for (ToolCall toolCall : decision.toolCalls()) {
                    ToolExecutionResult toolResult;
                    if (toolCallTracker.isRepeated(toolCall)) {
                        log.warn(
                                "Repeated tool call detected: tool={}, arguments={}",
                                toolCall.tool(),
                                toolCall.arguments());

                        toolResult = ToolExecutionResult.failure(
                                toolCall.id(),
                                toolCall.tool(),
                                "Repeated tool call detected. This tool was already "
                                        + "requested with the same arguments during this turn. "
                                        + "Use the previous result, try different arguments "
                                        + "if appropriate, or provide a final answer.");
                    } else {
                        log.info("Model called the tool: {}{}", toolCall.tool(), toolCall.arguments());
                        toolResult = executeTool(executionContext, toolCall);
                        context = context
                                .addMessage(LLMMessage.toolResult(toolCall.id(), buildToolResultMessage(toolResult)));
                    }
                }

                continue;
            }

            throw new IllegalStateException("Unknown agent decision type: " + decision.type());
        }

        log.warn("Agent reached maximum iterations: userId={}, iterations={}", userId, context.iteration());

        String finalResponse = responseModel.generate(buildIterationLimitResponseRequest(context));

        context = context.addMessage(new LLMMessage("assistant", finalResponse));

        List<LLMMessage> generatedMessages = new ArrayList<>(
                context.messages().subList(initialMessageCount, context.messages().size()));

        AgentResponse response = new AgentResponse(
                "final_answer",
                List.of(),
                finalResponse);

        return new AgentTurnResult(response, generatedMessages);
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

    private LLMRequest buildIterationLimitResponseRequest(AgentContext context) {
        List<LLMMessage> messages = new ArrayList<>(context.messages());

        messages.add(0, new LLMMessage(
                "system",
                """
                        The agent reached its maximum tool-execution iterations before
                        confirming that the user's request was fully completed.

                        Generate the best answer possible using only the information
                        already available in the conversation and tool results.

                        Do not invent missing information or claim that unfinished
                        operations succeeded. If the available results are incomplete,
                        briefly explain the limitation to the user.
                        """));

        return new LLMRequest(null, List.copyOf(messages), List.of());
    }

    private void validateToolCalls(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new IllegalStateException("Decision Model returned TOOL_CALLS without any tool calls");
        }

        Set<String> callIds = new HashSet<>();

        for (ToolCall toolCall : toolCalls) {
            if (toolCall == null) {
                throw new IllegalStateException("Decision Model returned a null tool call");
            }

            if (toolCall.id() == null || toolCall.id().isBlank()) {
                throw new IllegalStateException("Tool call ID must not be blank");
            }

            if (!callIds.add(toolCall.id())) {
                throw new IllegalStateException("Duplicate tool call ID: " + toolCall.id());
            }

            if (toolCall.tool() == null || toolCall.tool().isBlank()) {
                throw new IllegalStateException("Tool name must not be blank");
            }

            if (!toolRegistry.hasTool(toolCall.tool())) {
                throw new IllegalStateException("Decision Model requested an unknown tool: " + toolCall.tool());
            }
        }
    }

    private ToolExecutionResult executeTool(ToolExecutionContext executionContext, ToolCall toolCall) {
        try {
            String result = toolRegistry.execute(executionContext, toolCall.tool(), toolCall.arguments());
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