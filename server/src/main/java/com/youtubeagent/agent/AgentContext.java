package com.youtubeagent.agent;

import java.util.ArrayList;
import java.util.List;

import com.youtubeagent.ai.core.LLMMessage;

public record AgentContext(
        String userMessage,
        List<LLMMessage> messages,
        int iteration) {

    public AgentContext(String userMessage) {
        this(userMessage, List.of(new LLMMessage("user", userMessage)), 0);
    }

    public AgentContext {
        messages = List.copyOf(messages);
    }

    public AgentContext addMessage(LLMMessage message) {
        List<LLMMessage> updatedMessages = new ArrayList<>(messages);
        updatedMessages.add(message);
        return new AgentContext(userMessage, updatedMessages, iteration);
    }

    public AgentContext nextIteration() {
        return new AgentContext(userMessage, messages, iteration + 1);
    }
}