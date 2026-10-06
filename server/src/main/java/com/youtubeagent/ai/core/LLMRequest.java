package com.youtubeagent.ai.core;

import java.util.List;

public record LLMRequest(
        String model,
        List<LLMMessage> messages) {
}