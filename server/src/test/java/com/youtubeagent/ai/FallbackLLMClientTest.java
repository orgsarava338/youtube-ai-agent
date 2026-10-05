package com.youtubeagent.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.youtubeagent.config.AiProperties;

import org.junit.jupiter.api.Test;

class FallbackLLMClientTest {

    @Test
    void shouldUseFallbackProviderWhenPrimaryFails() {
        GroqClient groq = mock(GroqClient.class);
        GeminiClient gemini = mock(GeminiClient.class);
        HuggingFaceClient huggingFace = mock(HuggingFaceClient.class);
        OllamaClient ollama = mock(OllamaClient.class);

        when(groq.generate("hello")).thenThrow(new IllegalStateException("Groq unavailable"));
        when(gemini.generate("hello")).thenReturn("fallback answer");

        FallbackLLMClient client = new FallbackLLMClient(
                new AiProperties(List.of("groq", "gemini")),
                groq,
                gemini,
                huggingFace,
                ollama);

        String result = client.generate("hello");

        assertEquals("fallback answer", result);
        verify(groq).generate("hello");
        verify(gemini).generate("hello");
    }

    @Test
    void shouldThrowDetailedErrorWhenAllProvidersFail() {
        GroqClient groq = mock(GroqClient.class);
        GeminiClient gemini = mock(GeminiClient.class);
        HuggingFaceClient huggingFace = mock(HuggingFaceClient.class);
        OllamaClient ollama = mock(OllamaClient.class);

        when(groq.generate("hello")).thenThrow(new IllegalStateException("Groq unavailable"));
        when(gemini.generate("hello")).thenThrow(new IllegalStateException("Gemini unavailable"));

        FallbackLLMClient client = new FallbackLLMClient(
                new AiProperties(List.of("groq", "gemini")),
                groq,
                gemini,
                huggingFace,
                ollama);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.generate("hello"));

        assertTrue(ex.getMessage().contains("All configured LLM providers failed"));
        assertTrue(ex.getMessage().contains("groq"));
        assertTrue(ex.getMessage().contains("gemini"));
    }
}
