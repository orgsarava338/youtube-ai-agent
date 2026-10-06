package com.youtubeagent.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.youtubeagent.ai.model.ModelDefinition;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        List<String> fallbackOrder,
        List<ModelDefinition> models,
        Map<String, Map<String, String>> providers) {
}