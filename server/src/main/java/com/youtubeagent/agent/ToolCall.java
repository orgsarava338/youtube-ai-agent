package com.youtubeagent.agent;

import java.util.Map;

public record ToolCall(
        String id,
        String tool,
        Map<String, Object> arguments) {
}