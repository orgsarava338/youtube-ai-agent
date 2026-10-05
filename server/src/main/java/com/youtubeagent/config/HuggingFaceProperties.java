package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "huggingface")
public record HuggingFaceProperties(
        String baseUrl,
        String apiKey,
        String model) {
}