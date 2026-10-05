package com.youtubeagent.agent;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolRegistry {

    private final List<AgentTool> tools;

    public ToolRegistry(List<AgentTool> tools) {
        this.tools = tools;
    }

    public List<AgentTool> getTools() {
        return tools;
    }
}