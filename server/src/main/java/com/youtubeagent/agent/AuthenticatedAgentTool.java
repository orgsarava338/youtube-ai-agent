package com.youtubeagent.agent;

import java.util.Map;

public interface AuthenticatedAgentTool {

    String getName();

    String getDescription();

    Object execute(ToolExecutionContext executionContext, Map<String, Object> arguments);
}