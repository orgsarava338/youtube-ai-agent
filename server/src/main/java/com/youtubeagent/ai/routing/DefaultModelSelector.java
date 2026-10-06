package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.catalog.ModelCatalog;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;

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
    public List<ModelDefinition> select(ModelRequirements requirements) {

        return modelCatalog.getModels()
                .stream()
                .filter(model -> !requirements.freeOnly() || model.free())
                .filter(model -> model
                        .capabilities()
                        .containsAll(requirements.requiredCapabilities()))
                .sorted(Comparator.comparingInt(model -> model.priority()))
                .toList();
    }
}