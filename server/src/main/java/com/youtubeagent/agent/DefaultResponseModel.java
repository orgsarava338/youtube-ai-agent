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
public class DefaultResponseModel implements ResponseModel {

    private final LLMRouter llmRouter;

    public DefaultResponseModel(LLMRouter llmRouter) {
        this.llmRouter = llmRouter;
    }

    @Override
    public String generate(LLMRequest request) {

        LLMResponse response = llmRouter.generate(
                buildRequest(request),
                ModelRole.RESPONSE,
                new ModelRequirements(true, Set.of(ModelCapability.CHAT)));

        return response.content();
    }

    private LLMRequest buildRequest(LLMRequest request) {
        List<LLMMessage> messages = new ArrayList<>();

        messages.add(new LLMMessage("system", buildSystemPrompt()));
        messages.addAll(request.messages());

        return new LLMRequest(null, List.copyOf(messages), List.of());
    }

    private String buildSystemPrompt() {
        return """
                You are the Response Model for a YouTube AI agent.

                Your responsibility is to provide the final answer to the user's request
                using the conversation history and tool results provided by the application.

                Rules:
                - Answer the user's original request directly and clearly.
                - Use the information from tool results as authoritative.
                - Never invent, guess, or fabricate information.
                - Do not call any tools.
                - Do not suggest or output tool calls.
                - Never output `tool_code`.
                - Never output JSON representing a tool call.
                - Never output tool names as instructions to the application.
                - Do not ask the user to execute a tool.
                - If a tool failed, clearly explain the failure using the available information.
                - If the available tool results are insufficient to answer the request,
                  say what information is missing instead of making something up.
                - Do not mention internal model roles, agent iterations, routing,
                  model selection, or implementation details unless the user explicitly asks.
                - Return only the final human-readable response for the user.
                """;
    }
}