package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.gemini")
public record GeminiProperties(
        String apiKey,
        String baseUrl,
        String model) {
}