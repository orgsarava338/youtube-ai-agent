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
    private final ObjectMapper objectMapper;

    public AgentService(
            ToolRegistry toolRegistry,
            DecisionModel decisionModel,
            ObjectMapper objectMapper) {

        this.toolRegistry = toolRegistry;
        this.decisionModel = decisionModel;
        this.objectMapper = objectMapper;
    }

    public AgentResponse chat(String userMessage) {
        AgentContext context = new AgentContext(userMessage, buildSystemPrompt());

        while (context.iteration() < MAX_ITERATIONS) {
            context = context.nextIteration();
            log.info("Agent iteration: {}", context.iteration());

            LLMRequest request = buildRequest(context);
            AgentDecision decision = decisionModel.decide(request);

            if (decision.type() == AgentDecision.Type.FINAL_RESPONSE) {
                return new AgentResponse("final_answer", List.of(), decision.content());
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

    private String buildSystemPrompt() {
        return """
                You are an AI agent that can use tools.

                IMPORTANT:
                - Use tools when they are required to answer the user.
                - Tool results contain real data returned by the application.
                - Treat tool results as authoritative.
                - Never invent values that are available from tool results.

                TOOL EXECUTION RULES:
                - Do not repeat a tool call unless the previous tool call failed
                  or the returned data is insufficient.
                - If get_video successfully returns the requested video's details,
                  use that result instead of calling get_video again.
                - Do not call list_videos again after get_video succeeds.

                TOOL CALL SEQUENCING:
                - Multiple tool calls may be returned in the same response when
                  the calls are independent.
                - If a tool requires a value produced by another tool,
                  call the first tool alone.
                - Wait for its actual result before making the dependent call.
                - Always use the actual values returned by previous tools.
                - Never use placeholders, symbolic references, expressions,
                  or variable references for tool results.

                NEVER USE:
                {output_of_tool_name}
                {output_of_get_current_time}
                $tool_result
                output_of_tool
                or similar placeholder expressions.

                Never invent a value when the required value is available
                in a previous tool result.

                MULTI-TOOL EXAMPLES:
                - list_videos and list_playlists are independent and may be
                  called together.
                - list_playlists followed by get_playlist_videos is dependent.
                  Call list_playlists first, wait for the result, then call
                  get_playlist_videos using the actual playlistId.
                - get_current_time followed by get_channel_analytics is dependent
                  when calculating a relative date range. Call get_current_time
                  first, wait for the result, then call get_channel_analytics
                  using actual calculated dates.

                Available tools:
                %s
                """.formatted(
                toolRegistry.getToolDescriptions());
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