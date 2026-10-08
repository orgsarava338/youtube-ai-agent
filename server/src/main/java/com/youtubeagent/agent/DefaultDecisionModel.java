package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;
import com.youtubeagent.ai.routing.LLMRouter;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DefaultDecisionModel implements DecisionModel {

    private final LLMRouter llmRouter;
    private final ToolRegistry toolRegistry;

    public DefaultDecisionModel(LLMRouter llmRouter, ToolRegistry toolRegistry) {
        this.llmRouter = llmRouter;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentDecision decide(LLMRequest request) {

        LLMResponse response = llmRouter.generate(
                buildRequest(request),
                        ModelRole.DECISION,
                new ModelRequirements(
                        true,
                        Set.of(ModelCapability.TOOL_CALLING, ModelCapability.JSON)));

        if (response.hasToolCalls()) {
            List<ToolCall> toolCalls = response.toolCalls()
                    .stream()
                    .map(toolCall -> new ToolCall(
                            toolCall.id(),
                            toolCall.name(),
                            toolCall.arguments()))
                    .toList();

            return AgentDecision.toolCalls(toolCalls);
        }

        if (response.content() != null && !response.content().isBlank()) {
            return AgentDecision.finalResponse(response.content());
        }

        throw new IllegalStateException("Decision model returned neither tool calls nor content");
    }

    private LLMRequest buildRequest(LLMRequest request) {
        List<LLMMessage> messages = new ArrayList<>();

        messages.add(new LLMMessage("system", buildSystemPrompt()));
        messages.addAll(request.messages());

        return new LLMRequest(null, List.copyOf(messages), toolRegistry.getLLMToolDefinitions());
    }

    private String buildSystemPrompt() {
        return """
                You are the Decision Model for a YouTube AI agent.

                Your responsibility is to decide what the application should do next
                in order to complete the user's request.

                Available tools:
                %s

                TOOL EXECUTION:

                - Tool calls are executed by the application.
                - After a tool call, the application provides the actual result in the
                  conversation history.
                - Inspect previous tool results before deciding what to do next.
                - Use actual values returned by previous tools.
                - Never invent, guess, or fabricate values.

                TOOL DEPENDENCIES:

                - If a tool requires a value produced by another tool, call the first
                  tool and wait for its actual result.
                - Then use the exact value returned by that tool.
                - Never use placeholders, symbolic references, expressions, or invented IDs.

                For example:

                - If the user asks for videos in a playlist and provides only a playlist
                  name, first use list_playlists to find the actual playlistId.
                - After list_playlists returns, use the actual playlistId with
                  get_playlist_videos.
                - If get_playlist_videos returns video IDs and additional video information
                  is required, use an actual returned videoId with get_video.
                - Never invent a videoId or playlistId.

                TOOL SELECTION:

                - Use a tool whenever real YouTube/application data is required.
                - Use the available tools that can directly provide the requested information.
                - Do not invent tools that are not listed in Available tools.
                - Do not describe a hypothetical tool call as text.
                - Use the native tool-calling mechanism.
                - For independent operations, multiple tool calls may be made together.
                - For dependent operations, call the first tool and wait for its result.

                TOOL STATE:

                - Do not repeat a successful tool call when its result is already available.
                - If a previous tool result contains the information required for the next
                  step, use that information.
                - Do not replace real returned values with placeholders.
                - If a tool failed, inspect the failure and decide whether another available
                  tool can complete the request.
                - Continue the workflow until the user's request is complete.

                NEVER:

                - Invent tool names.
                - Invent tool arguments.
                - Invent IDs.
                - Guess IDs.
                - Use placeholder IDs.
                - Use symbolic references such as:
                  {output_of_tool}
                  {output_of_list_playlists}
                  video_id_1
                  playlist_id_1
                - Write tool calls as text.
                - Write tool calls as JSON.
                - Write Markdown pretending to be a tool call.
                - Output tool_code.
                - Ask the user to execute a tool.

                FINAL STATE:

                When all information required to complete the user's request has been
                obtained from the conversation and tool results, stop calling tools.

                Return FINAL_RESPONSE only when no additional tool call is required.

                If additional information can be obtained using an available tool,
                continue by calling that tool.

                Always base the decision on:

                1. The user's original request.
                2. The conversation history.
                3. The actual results returned by previous tools.
                4. The tools currently available.
                """.formatted(toolRegistry.getToolDescriptions());
                    }

}