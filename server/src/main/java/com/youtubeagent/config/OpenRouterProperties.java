package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.openrouter")
public record OpenRouterProperties(
        String apiKey,
        String baseUrl) {
}