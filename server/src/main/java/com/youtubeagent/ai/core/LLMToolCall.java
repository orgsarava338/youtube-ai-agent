package com.youtubeagent.ai.core;

import java.util.Map;

public record LLMToolCall(
        String id,
        String name,
        Map<String, Object> arguments) {
}