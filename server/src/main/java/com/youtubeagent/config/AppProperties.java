package com.youtubeagent.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        List<String> allowedOrigins) {

    public AppProperties(String allowedOrigins) {
        this(
                Arrays.stream(allowedOrigins.split(","))
                        .map(origin -> origin.trim())
                        .filter(origin -> !origin.isBlank())
                        .filter(origin -> origin.startsWith("https://"))
                        .toList());
    }
}