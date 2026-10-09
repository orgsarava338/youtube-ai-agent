package com.youtubeagent.ai.catalog;

import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlModelCatalogTest {

    @Test
    void returnsConfiguredModelsWithoutChangingOrder() {
        ModelDefinition first = new ModelDefinition("model-a", "openai", true,
                Set.of(ModelCapability.CHAT), 1);
        ModelDefinition second = new ModelDefinition("model-b", "ollama", false,
                Set.of(ModelCapability.TOOL_CALLING), 2);
        YamlModelCatalog catalog = new YamlModelCatalog(new AiProperties(List.of(first, second)));

        assertEquals(List.of(first, second), catalog.getModels());
    }
}
