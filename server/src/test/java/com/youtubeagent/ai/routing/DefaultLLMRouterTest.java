package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelCapability;
import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.provider.LLMProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultLLMRouterTest {

    @Test
    void shouldUseFirstAvailableModel() {

        ModelSelector selector = mock(ModelSelector.class);

        LLMProvider provider = mock(LLMProvider.class);

        ModelDefinition model = model(
                "model-a",
                "provider-a",
                10);

        when(selector.select(any()))
                .thenReturn(List.of(model));

        when(provider.getProviderId())
                .thenReturn("provider-a");

        when(provider.generate(any()))
                .thenReturn(new LLMResponse(
                        "hello",
                        "provider-a",
                        "model-a"));

        DefaultLLMRouter router = new DefaultLLMRouter(
                selector,
                List.of(provider));

        LLMResponse result = router.generate(
                new LLMRequest(
                        null,
                        List.of(new LLMMessage("user", "hello"))),
                new ModelRequirements(true, Set.of()));

        assertEquals("hello", result.content());

        verify(provider).generate(any());
    }

    @Test
    void shouldFallbackToNextModelWhenFirstFails() {

        ModelSelector selector = mock(ModelSelector.class);

        LLMProvider firstProvider = mock(LLMProvider.class);
        LLMProvider secondProvider = mock(LLMProvider.class);

        ModelDefinition firstModel = model("model-a", "provider-a", 10);

        ModelDefinition secondModel = model("model-b", "provider-b", 20);

        when(selector.select(any()))
                .thenReturn(List.of(firstModel, secondModel));

        when(firstProvider.getProviderId())
                .thenReturn("provider-a");

        when(secondProvider.getProviderId())
                .thenReturn("provider-b");

        when(firstProvider.generate(any()))
                .thenThrow(new RuntimeException("provider failed"));

        when(secondProvider.generate(any()))
                .thenReturn(new LLMResponse(
                        "fallback worked",
                        "provider-b",
                        "model-b"));

        DefaultLLMRouter router = new DefaultLLMRouter(
                selector,
                List.of(firstProvider, secondProvider));

        LLMResponse result = router.generate(
                new LLMRequest(
                        null,
                        List.of(new LLMMessage("user", "hello"))),
                new ModelRequirements(true, Set.of()));

        assertEquals("fallback worked", result.content());

        verify(firstProvider).generate(any());
        verify(secondProvider).generate(any());
    }

    @Test
    void shouldFailWhenAllModelsFail() {

        ModelSelector selector = mock(ModelSelector.class);

        LLMProvider provider = mock(LLMProvider.class);

        ModelDefinition model = model("model-a", "provider-a", 10);

        when(selector.select(any()))
                .thenReturn(List.of(model));

        when(provider.getProviderId())
                .thenReturn("provider-a");

        when(provider.generate(any()))
                .thenThrow(new RuntimeException("failure"));

        DefaultLLMRouter router = new DefaultLLMRouter(
                selector,
                List.of(provider));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> router.generate(
                        new LLMRequest(
                                null,
                                List.of(new LLMMessage(
                                        "user",
                                        "hello"))),
                        new ModelRequirements(
                                true,
                                Set.of())));

        assertEquals(
                "All suitable LLM models failed",
                exception.getMessage());
    }

    @Test
    void shouldSkipModelWhenProviderIsNotRegistered() {

        ModelSelector selector = mock(ModelSelector.class);

        ModelDefinition model = model("model-a", "missing-provider", 10);

        when(selector.select(any()))
                .thenReturn(List.of(model));

        DefaultLLMRouter router = new DefaultLLMRouter(selector, List.of());

        assertThrows(
                IllegalStateException.class,
                () -> router.generate(
                        new LLMRequest(
                                null,
                                List.of(new LLMMessage(
                                        "user",
                                        "hello"))),
                        new ModelRequirements(
                                true,
                                Set.of())));
    }

    private ModelDefinition model(
            String id,
            String provider,
            int priority) {

        return new ModelDefinition(
                id,
                provider,
                true,
                Set.of(ModelCapability.CHAT),
                priority);
    }
}