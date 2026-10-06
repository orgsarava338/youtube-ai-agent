package com.youtubeagent.ai.catalog;

import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.config.AiProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YamlModelCatalog implements ModelCatalog {

    private final AiProperties properties;

    public YamlModelCatalog(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<ModelDefinition> getModels() {
        return properties.models();
    }
}