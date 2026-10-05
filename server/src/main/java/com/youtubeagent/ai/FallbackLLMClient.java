package com.youtubeagent.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.youtubeagent.config.AiProperties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class FallbackLLMClient implements LLMClient {

    private final List<NamedClient> clients = new ArrayList<>();

    public FallbackLLMClient(AiProperties properties, GroqClient groq, GeminiClient gemini, HuggingFaceClient huggingface, OllamaClient ollama) {
        Map<String, LLMClient> available = Map.of(
                "groq", groq,
                "gemini", gemini,
                "huggingface", huggingface,
                "ollama", ollama);

        for (String raw : properties.fallbackOrder()) {
            String name = raw.trim().toLowerCase();
            if (name.isEmpty()) {
                continue;
            }
            LLMClient client = available.get(name);
            if (client == null) {
                throw new IllegalStateException(
                        "Unknown provider '%s' in ai.fallback-order. Valid values: %s"
                                .formatted(name, available.keySet()));
            }
            clients.add(new NamedClient(name, client));
        }

        if (clients.isEmpty()) {
            throw new IllegalStateException("ai.fallback-order must contain at least one provider");
        }

        log.info("LLM fallback order: {}", clients.stream().map(client -> client.name()).toList());
    }

    @Override
    public String generate(String prompt) {
        RuntimeException lastError = null;

        for (NamedClient client : clients) {
            try {
                String result = client.delegate().generate(prompt);
                if (lastError != null) {
                    log.info("Succeeded with fallback provider: {}", client.name());
                }
                return result;
            } catch (RuntimeException e) {
                log.warn("LLM provider '{}' failed: {}", client.name(), e.getMessage());
                lastError = e;
            }
        }

        throw new IllegalStateException("All LLM providers failed", lastError);
    }

    private record NamedClient(String name, LLMClient delegate) {
    }
}