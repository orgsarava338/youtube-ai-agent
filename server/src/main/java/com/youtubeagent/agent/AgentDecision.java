package com.youtubeagent.agent;

import java.util.List;

public record AgentDecision(
        Type type,
        String content,
        List<ToolCall> toolCalls) {

    public enum Type {
        TOOL_CALLS,
        FINAL_RESPONSE
    }

    public static AgentDecision toolCalls(List<ToolCall> toolCalls) {
        return new AgentDecision(
                Type.TOOL_CALLS,
                null,
                toolCalls);
    }

    public static AgentDecision finalResponse(String content) {
        return new AgentDecision(
                Type.FINAL_RESPONSE,
                content,
                List.of());
    }
}