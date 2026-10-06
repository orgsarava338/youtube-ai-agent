package com.youtubeagent.agent;

import java.util.List;
import java.util.Map;

public record AgentResponse(
        String type,
        List<ToolCall> calls,
        String content) {

    public record ToolCall(
            String tool,
            Map<String, Object> arguments) {
    }
}
