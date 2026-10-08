package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.routing.LLMRouter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AgentService {

    private static final int MAX_ITERATIONS = 5;

    private final LLMRouter llmRouter;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentService(LLMRouter llmRouter, ToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.llmRouter = llmRouter;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    public AgentResponse chat(String userMessage) {

        List<LLMMessage> messages = new ArrayList<>();

        messages.add(new LLMMessage("system", buildSystemPrompt()));
        messages.add(new LLMMessage("user", userMessage));

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            log.info("Agent iteration: {}", iteration + 1);

            ModelRequirements requirements = new ModelRequirements(true, Set.of());
            LLMRequest request = new LLMRequest(null, List.copyOf(messages));
            LLMResponse response = llmRouter.generate(request, requirements);

            messages.add(new LLMMessage("assistant", response.content()));

            AgentResponse agentResponse = parseResponse(response.content());

            if ("final_answer".equals(agentResponse.type())) {
                return agentResponse;
            }

            if ("tool_calls".equals(agentResponse.type())) {
                for (AgentResponse.ToolCall call : agentResponse.calls()) {
                    String toolResult = executeTool(call);
                    messages.add(new LLMMessage("user", buildToolResultMessage(call.tool(), toolResult)));
                }
                continue;
            }

            throw new IllegalStateException("Unknown agent response type: " + agentResponse.type());
        }

        throw new IllegalStateException("Agent exceeded maximum iterations");
    }

    private String buildSystemPrompt() {
        return """
                You are an AI agent that can use tools.

                You must respond using valid JSON.

                IMPORTANT:
                - Return exactly ONE JSON object.
                - Never return multiple JSON objects.
                - Do not add markdown or explanations outside the JSON object.

                JSON FORMATTING RULES:
                - The response must always be valid JSON.
                - Never put literal newlines inside a JSON string.
                - If a string needs a line break, use the escaped sequence \\n.
                - Escape double quotes inside string values as \\".
                - Do not use trailing commas.

                TOOL EXECUTION RULES:
                - After a tool returns a result, treat that result as authoritative.
                - Do not repeat a tool call unless the previous tool call failed
                  or the returned data is insufficient to answer the user.
                - If get_video successfully returns the requested video's details,
                  use that result to answer the user directly.
                - Do not call list_videos again after get_video succeeds.

                TOOL CALL SEQUENCING:
                - Multiple tool calls may be returned in the same response when
                  the calls are independent of each other.
                - If a tool call requires a value produced by another tool,
                  do not call both tools in the same response.
                - Call the first tool, wait for its actual result, and then call
                  the dependent tool in a later response.
                - Always use actual values returned by previous tool calls.
                - Never use placeholders, symbolic references, expressions, or
                  variable references for tool results.
                - Never use values such as:
                  {output_of_tool_name}
                  {output_of_get_current_time}
                  $tool_result
                  output_of_tool
                  or similar constructs.
                - Never invent a value when the required value is available from
                  a previous tool result.

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

                When one or more independent tools are required, use:

                {
                  "type": "tool_calls",
                  "calls": [
                    {
                      "tool": "tool_name",
                      "arguments": {}
                    }
                  ]
                }

                When you have the final answer, use:

                {
                  "type": "final_answer",
                  "content": "your answer"
                }

                Available tools:
                %s
                """.formatted(toolRegistry.getToolDescriptions());
    }

    private String buildToolResultMessage(String tool, String toolResult) {

        try {
            return objectMapper.writeValueAsString(
                    Map.of(
                            "type", "tool_result",
                            "tool", tool,
                            "result", toolResult,
                            "instructions",
                            "The result above is the actual output returned "
                                    + "by the application. Use the exact values "
                                    + "from this result when making subsequent "
                                    + "tool calls. Never use placeholders, "
                                    + "symbolic references, or expressions for "
                                    + "tool results. If another tool requires "
                                    + "a value from this result, make that tool "
                                    + "call in a later response using the actual "
                                    + "value."));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build tool result message", e);
        }
    }

    private AgentResponse parseResponse(String content) {
        try {
            return objectMapper.readValue(content, AgentResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", content, e);
            throw new IllegalStateException("Invalid LLM response", e);
        }
    }

    private String executeTool(AgentResponse.ToolCall call) {
        try {
            return toolRegistry.execute(call.tool(), call.arguments());
        } catch (Exception e) {
            log.error("Tool execution failed: {}", call.tool(), e);
            return "Tool execution failed: " + e.getMessage();
        }
    }
}