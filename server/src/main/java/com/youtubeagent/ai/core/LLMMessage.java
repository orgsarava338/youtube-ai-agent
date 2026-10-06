package com.youtubeagent.ai.core;

public record LLMMessage(
        String role,
        String content) {
}