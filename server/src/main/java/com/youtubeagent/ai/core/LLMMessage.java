package com.youtubeagent.ai.core;

import java.util.List;

public record LLMMessage(
        String role,
        String content,
        List<LLMToolCall> toolCalls,
        String toolCallId) {

    public LLMMessage(String role, String content) {
        this(role, content, List.of(), null);
    }

    public static LLMMessage assistantToolCalls(List<LLMToolCall> toolCalls) {
        return new LLMMessage("assistant", null, toolCalls, null);
    }

    public static LLMMessage toolResult(String toolCallId, String content) {
        return new LLMMessage("tool", content, List.of(), toolCallId);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}