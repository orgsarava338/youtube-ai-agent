package com.youtubeagent.ai.core;

import java.util.List;

public record LLMResponse(
        String content,
        String provider,
        String model, 
        List<LLMToolCall> toolCalls) {

    public LLMResponse(
            String content,
            String provider,
            String model) {

        this(content, provider, model, List.of());
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}