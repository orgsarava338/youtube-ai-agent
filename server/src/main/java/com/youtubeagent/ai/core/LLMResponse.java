package com.youtubeagent.ai.core;

public record LLMResponse(
        String content,
        String provider,
        String model) {
}