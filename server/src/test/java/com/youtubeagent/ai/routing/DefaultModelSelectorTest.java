package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.catalog.ModelCatalog;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultModelSelectorTest {

    @Test
    void shouldSortModelsByPriority() {
        ModelCatalog catalog = mock(ModelCatalog.class);

        ModelDefinition slow = model(
                "model-slow",
                "provider-a",
                false,
                20,
                ModelCapability.CHAT);

        ModelDefinition fast = model(
                "model-fast",
                "provider-a",
                false,
                10,
                ModelCapability.CHAT);

        when(catalog.getModels()).thenReturn(List.of(slow, fast));

        DefaultModelSelector selector = new DefaultModelSelector(catalog);

        List<ModelDefinition> result = selector.select(
                ModelRole.RESPONSE,
                new ModelRequirements(false, Set.of(ModelCapability.CHAT)));

        assertEquals(List.of(fast, slow), result);
    }

    @Test
    void shouldFilterFreeModels() {
        ModelCatalog catalog = mock(ModelCatalog.class);

        ModelDefinition free = model(
                "free-model",
                "provider-a",
                true,
                10,
                ModelCapability.CHAT);

        ModelDefinition paid = model(
                "paid-model",
                "provider-a",
                false,
                20,
                ModelCapability.CHAT);

        when(catalog.getModels()).thenReturn(List.of(paid, free));

        DefaultModelSelector selector = new DefaultModelSelector(catalog);

        List<ModelDefinition> result = selector.select(
                ModelRole.RESPONSE,
                new ModelRequirements(true, Set.of(ModelCapability.CHAT)));

        assertEquals(List.of(free), result);
    }

    @Test
    void shouldFilterModelsWithoutRequiredCapabilities() {
        ModelCatalog catalog = mock(ModelCatalog.class);

        ModelDefinition chatOnly = model(
                "chat-model",
                "provider-a",
                true,
                10,
                ModelCapability.CHAT);

        ModelDefinition toolModel = model(
                "tool-model",
                "provider-a",
                true,
                20,
                ModelCapability.CHAT,
                ModelCapability.JSON,
                ModelCapability.TOOL_CALLING);

        when(catalog.getModels()).thenReturn(List.of(chatOnly, toolModel));

        DefaultModelSelector selector = new DefaultModelSelector(catalog);

        List<ModelDefinition> result = selector.select(
                ModelRole.DECISION,
                new ModelRequirements(
                        true,
                        Set.of(ModelCapability.TOOL_CALLING)));

        assertEquals(List.of(toolModel), result);
    }

    @Test
    void shouldReturnEmptyWhenNoModelMatches() {
        ModelCatalog catalog = mock(ModelCatalog.class);

        ModelDefinition model = model(
                "chat-model",
                "provider-a",
                true,
                10,
                ModelCapability.CHAT);

        when(catalog.getModels()).thenReturn(List.of(model));

        DefaultModelSelector selector = new DefaultModelSelector(catalog);

        List<ModelDefinition> result = selector.select(
                ModelRole.RESPONSE,
                new ModelRequirements(
                        true,
                        Set.of(ModelCapability.VISION)));

        assertTrue(result.isEmpty());
    }

    private ModelDefinition model(
            String id,
            String provider,
            boolean free,
            int priority,
            ModelCapability... capabilities) {

        return new ModelDefinition(
                id,
                provider,
                free,
                Set.of(capabilities),
                priority);
    }
}