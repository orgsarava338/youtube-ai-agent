package com.youtubeagent.agent;

import java.util.Map;

public record AgentResponse(
        String type,
        String tool,
        Map<String, Object> arguments,
        String content) {
}