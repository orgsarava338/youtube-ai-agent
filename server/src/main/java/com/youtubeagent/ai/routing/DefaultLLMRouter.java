package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.provider.LLMProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public LLMResponse generate(LLMRequest request, ModelRequirements requirements) {

        List<ModelDefinition> models = modelSelector.select(requirements);

        Exception lastException = null;

        for (ModelDefinition model : models) {
            LLMProvider provider = providers.get(model.provider());

            if (provider == null) {
                continue;
            }

            try {
                LLMRequest modelRequest = new LLMRequest(model.id(), request.messages());
                return provider.generate(modelRequest);
            } catch (Exception exception) {
                lastException = exception;
            }
        }

        throw new IllegalStateException("All suitable LLM models failed", lastException);
    }
}