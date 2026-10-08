package com.youtubeagent.ai.core;

import java.util.Map;

public record LLMToolDefinition(
        String name,
        String description,
        Map<String, Object> parameters) {
}