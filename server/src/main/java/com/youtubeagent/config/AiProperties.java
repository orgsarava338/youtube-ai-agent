package com.youtubeagent.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.youtubeagent.ai.model.ModelDefinition;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        List<ModelDefinition> models) {
}