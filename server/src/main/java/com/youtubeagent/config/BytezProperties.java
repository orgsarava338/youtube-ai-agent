package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.bytez")
public record BytezProperties(
        String apiKey,
        String baseUrl) {
}