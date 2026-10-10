package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMMessage;

import java.util.List;

public record AgentTurnResult(
        AgentResponse response,
        List<LLMMessage> messages) {

    public AgentTurnResult {
        messages = List.copyOf(messages);
    }
}