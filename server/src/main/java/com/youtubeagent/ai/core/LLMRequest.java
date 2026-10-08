package com.youtubeagent.ai.core;

import java.util.List;

public record LLMRequest(
        String model,
        List<LLMMessage> messages,
        List<LLMToolDefinition> tools) {

    public boolean hasTools() {
        return tools != null && !tools.isEmpty();
    }
}