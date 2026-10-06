package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.ollama")
public record OllamaProperties(
        String baseUrl,
        String model) {
}