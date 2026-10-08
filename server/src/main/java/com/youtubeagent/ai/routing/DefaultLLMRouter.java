package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;
import com.youtubeagent.ai.provider.LLMProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultLLMRouter implements LLMRouter {

    private final ModelSelector modelSelector;
    private final Map<String, LLMProvider> providers;

    public DefaultLLMRouter(ModelSelector modelSelector, List<LLMProvider> providerList) {
        this.modelSelector = modelSelector;
        this.providers = providerList
                .stream()
                .collect(Collectors.toMap(provider -> provider.getProviderId(), provider -> provider));
    }

    @Override
    public LLMResponse generate(LLMRequest request, ModelRole role, ModelRequirements requirements) {

        List<ModelDefinition> models = modelSelector.select(role, requirements);

        Exception lastException = null;

        for (ModelDefinition model : models) {
            LLMProvider provider = providers.get(model.provider());

            if (provider == null) {
                continue;
            }

            try {
                LLMRequest modelRequest = new LLMRequest(model.id(), request.messages(), request.tools());
                log.info("LLM call started role={} model={} provider={}", role, model.id(), model.provider());
                return provider.generate(modelRequest);
            } catch (Exception exception) {
                lastException = exception;
            }
        }

        throw new IllegalStateException("All suitable LLM models failed", lastException);
    }
}