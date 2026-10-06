package com.youtubeagent.config;

import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.provider.LLMProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiConfigurationValidator {

    private final AiProperties aiProperties;
    private final Map<String, LLMProvider> providers;

    public AiConfigurationValidator(AiProperties aiProperties, List<LLMProvider> providerList) {
        this.aiProperties = aiProperties;

        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getProviderId().toLowerCase(),
                        Function.identity()));
    }

    @PostConstruct
    public void validate() {
        validateModels();
        log.info("AI configuration validation completed successfully");
    }

    private void validateModels() {
        List<ModelDefinition> models = aiProperties.models();

        if (models == null || models.isEmpty()) {
            throw new IllegalStateException("At least one AI model must be configured");
        }

        Set<String> modelIds = new HashSet<>();

        for (ModelDefinition model : models) {
            validateModel(model);
            if (!modelIds.add(model.id())) {
                throw new IllegalStateException("Duplicate AI model configured: " + model.id());
            }
        }
    }

    private void validateModel(ModelDefinition model) {

        if (model.id() == null || model.id().isBlank()) {
            throw new IllegalStateException("AI model id must not be empty");
        }

        if (model.provider() == null || model.provider().isBlank()) {
            throw new IllegalStateException("AI model provider must not be empty for model: " + model.id());
        }

        validateProviderExists(model.provider(), "model " + model.id());
    }

    private void validateProviderExists(String providerId, String source) {

        if (!providers.containsKey(providerId.toLowerCase())) {
            throw new IllegalStateException(
                    "AI provider '" + providerId
                            + "' referenced by " + source
                            + " is not configured");
        }
    }
}