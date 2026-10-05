package com.youtubeagent.agent;

import java.util.Map;

public interface AgentTool {

    String getName();

    String getDescription();

    Object execute(Map<String, Object> arguments);
}