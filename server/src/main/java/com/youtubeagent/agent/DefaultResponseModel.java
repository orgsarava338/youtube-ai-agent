package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;
import com.youtubeagent.ai.routing.LLMRouter;
import org.springframework.stereotype.Service;

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
                request,
                ModelRole.RESPONSE,
                new ModelRequirements(
                        true,
                        Set.of(ModelCapability.CHAT)));

        return response.content();
    }
}