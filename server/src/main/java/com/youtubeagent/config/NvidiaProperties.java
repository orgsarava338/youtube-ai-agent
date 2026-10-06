package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.providers.nvidia")
public record NvidiaProperties(
        String apiKey,
        String baseUrl) {
}