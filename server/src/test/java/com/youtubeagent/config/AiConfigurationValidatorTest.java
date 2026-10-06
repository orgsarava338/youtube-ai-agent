package com.youtubeagent.config;

import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.provider.LLMProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiConfigurationValidatorTest {

    @Test
    void shouldAcceptValidConfiguration() {

        ModelDefinition model = model("model-a", "openrouter");

        AiProperties properties = new AiProperties(
                List.of(model));

        LLMProvider provider = provider("openrouter");

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of(provider));

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void shouldRejectWhenNoModelsConfigured() {

        AiProperties properties = new AiProperties(
                List.of());

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                validator::validate);

        assertEquals(
                "At least one AI model must be configured",
                exception.getMessage());
    }

    @Test
    void shouldRejectModelWithoutId() {

        ModelDefinition model = new ModelDefinition(
                "",
                "openrouter",
                true,
                Set.of(ModelCapability.CHAT),
                10);

        AiProperties properties = new AiProperties(
                List.of(model));

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of(provider("openrouter")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                validator::validate);

        assertEquals(
                "AI model id must not be empty",
                exception.getMessage());
    }

    @Test
    void shouldRejectModelWithoutProvider() {

        ModelDefinition model = new ModelDefinition(
                "model-a",
                "",
                true,
                Set.of(ModelCapability.CHAT),
                10);

        AiProperties properties = new AiProperties(
                List.of(model));

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                validator::validate);

        assertTrue(
                exception.getMessage()
                        .contains("AI model provider must not be empty"));
    }

    @Test
    void shouldRejectUnknownProvider() {

        ModelDefinition model = model("model-a", "missing-provider");

        AiProperties properties = new AiProperties(
                List.of(model));

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of(provider("openrouter")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                validator::validate);

        assertTrue(
                exception.getMessage()
                        .contains("missing-provider"));
    }

    @Test
    void shouldRejectDuplicateModelIds() {

        ModelDefinition first = model("duplicate-model", "openrouter");

        ModelDefinition second = model("duplicate-model", "openrouter");

        AiProperties properties = new AiProperties(
                List.of(first, second));

        AiConfigurationValidator validator = new AiConfigurationValidator(
                properties,
                List.of(provider("openrouter")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                validator::validate);

        assertTrue(
                exception.getMessage()
                        .contains("Duplicate AI model configured"));
    }

    private ModelDefinition model(
            String id,
            String provider) {

        return new ModelDefinition(
                id,
                provider,
                true,
                Set.of(ModelCapability.CHAT),
                10);
    }

    private LLMProvider provider(String id) {

        LLMProvider provider = mock(LLMProvider.class);

        when(provider.getProviderId())
                .thenReturn(id);

        return provider;
    }
}