package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.catalog.ModelCatalog;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class DefaultModelSelector implements ModelSelector {

    private final ModelCatalog modelCatalog;

    public DefaultModelSelector(ModelCatalog modelCatalog) {
        this.modelCatalog = modelCatalog;
    }

    @Override
    public List<ModelDefinition> select(ModelRole role, ModelRequirements requirements) {

        return modelCatalog.getModels()
                .stream()
                .filter(model -> !requirements.freeOnly() || model.free())
                .filter(model -> model
                        .capabilities()
                        .containsAll(requirements.requiredCapabilities()))
                .filter(model -> supportsRole(model, role))
                .sorted(Comparator.comparingInt(model -> model.priority()))
                .toList();
    }

    private boolean supportsRole(ModelDefinition model, ModelRole role) {

        return switch (role) {
            case DECISION ->
                model.capabilities().contains(ModelCapability.TOOL_CALLING)
                        && model.capabilities().contains(ModelCapability.JSON);

            case RESPONSE ->
                model.capabilities().contains(ModelCapability.CHAT);
        };
    }
}