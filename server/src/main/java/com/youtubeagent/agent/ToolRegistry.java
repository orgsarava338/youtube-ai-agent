package com.youtubeagent.agent;

import org.springframework.stereotype.Component;

import com.youtubeagent.ai.core.LLMToolDefinition;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ToolRegistry {

    private final List<AgentTool> tools;

    public ToolRegistry(List<AgentTool> tools) {
        this.tools = tools;
    }

    public List<AgentTool> getTools() {
        return tools;
    }

    public List<LLMToolDefinition> getLLMToolDefinitions() {
        if (tools == null || tools.isEmpty()) {
            return List.of();
        }

        return tools.stream()
                .map(tool -> new LLMToolDefinition(
                        tool.getName(),
                        tool.getDescription(),
                        Map.of(
                                "type", "object",
                                "additionalProperties", true)))
                .toList();
    }

    public String getToolDescriptions() {
        if (tools == null || tools.isEmpty()) {
            return "No tools available.";
        }

        StringBuilder description = new StringBuilder();
        description.append("Tool Name | Description\n");
        description.append("--- | ---\n");

        for (AgentTool tool : tools) {
            description.append(tool.getName())
                    .append(" | ")
                    .append(tool.getDescription())
                    .append(System.lineSeparator());
        }

        return description.toString();
    }

    public String execute(String toolName, Map<String, Object> arguments) {
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required.");
        }

        AgentTool tool = tools.stream()
                .filter(candidate -> candidate.getName().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));

        Object result = tool.execute(arguments == null ? Map.of() : arguments);

        return result == null ? "" : result.toString();
    }

}