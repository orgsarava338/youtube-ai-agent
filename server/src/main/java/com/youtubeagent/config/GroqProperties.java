package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.groq")
public record GroqProperties(
        String apiKey,
        String baseUrl,
        String model) {
}