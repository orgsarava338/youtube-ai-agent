package com.youtubeagent.agent;

import java.util.List;

public record AgentDecision(
        Type type,
        List<ToolCall> toolCalls) {

    public enum Type {
        TOOL_CALLS,
        DONE
    }

    public static AgentDecision toolCalls(List<ToolCall> toolCalls) {
        return new AgentDecision(
                Type.TOOL_CALLS,
                toolCalls);
    }

    public static AgentDecision done() {
        return new AgentDecision(
                Type.DONE,
                List.of());
    }
}