package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;
import com.youtubeagent.ai.routing.LLMRouter;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DefaultDecisionModel implements DecisionModel {

    private final LLMRouter llmRouter;

    public DefaultDecisionModel(LLMRouter llmRouter) {
        this.llmRouter = llmRouter;
    }

    @Override
    public AgentDecision decide(LLMRequest request) {

        LLMResponse response = llmRouter.generate(
                request,
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
}