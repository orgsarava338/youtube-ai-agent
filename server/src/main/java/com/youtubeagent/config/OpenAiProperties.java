package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.openai")
public record OpenAiProperties(
        String apiKey,
        String baseUrl,
        String model) {
}