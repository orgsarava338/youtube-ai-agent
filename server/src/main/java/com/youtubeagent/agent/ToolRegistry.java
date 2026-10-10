package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ToolRegistry {

    private final List<Object> tools;
    private final ObjectMapper objectMapper;

    public ToolRegistry(
            List<AgentTool> publicTools,
            List<AuthenticatedAgentTool> authenticatedTools,
            ObjectMapper objectMapper) {

        this.tools = new java.util.ArrayList<>();
        this.tools.addAll(publicTools);
        this.tools.addAll(authenticatedTools);
        this.objectMapper = objectMapper;

        log.info("Public tools: {}", publicTools.stream().map(tool -> tool.getName()));
        log.info("Authenticated Tools: {}", authenticatedTools.stream().map(tool -> tool.getName()));
    }

    public List<Object> getTools() {
        return List.copyOf(tools);
    }

    public List<LLMToolDefinition> getLLMToolDefinitions() {
        return tools.stream()
                .map(tool -> new LLMToolDefinition(
                        getName(tool),
                        getDescription(tool),
                        Map.of(
                                "type", "object",
                                "additionalProperties", true)))
                .toList();
    }

    public String getToolDescriptions() {
        if (tools.isEmpty()) {
            return "No tools available.";
        }

        StringBuilder description = new StringBuilder();
        description.append("Tool Name | Description\n");
        description.append("--- | ---\n");

        for (Object tool : tools) {
            description.append(getName(tool))
                    .append(" | ")
                    .append(getDescription(tool))
                    .append(System.lineSeparator());
        }

        return description.toString();
    }

    public String execute(ToolExecutionContext executionContext, String toolName, Map<String, Object> arguments) {

        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required.");
        }

        Object tool = tools.stream()
                .filter(candidate -> getName(candidate).equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));

        Map<String, Object> safeArguments = arguments == null ? Map.of() : arguments;

        Object result;

        if (tool instanceof AuthenticatedAgentTool authenticatedTool) {
            if (executionContext == null) {
                throw new IllegalStateException("Execution context is required for authenticated tools.");
            }

            executionContext.requireUserId();
            result = authenticatedTool.execute(executionContext, safeArguments);

        } else if (tool instanceof AgentTool publicTool) {
            result = publicTool.execute(safeArguments);

        } else {
            throw new IllegalStateException(
                    "Unsupported tool type: " + tool.getClass().getName());
        }

        try {
            if (result == null) {
                return "";
            }

            return result instanceof String stringResult
                    ? stringResult
                    : objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize result from tool: " + toolName,
                    e);
        }
    }

    private String getName(Object tool) {
        if (tool instanceof AgentTool publicTool) {
            return publicTool.getName();
        }

        if (tool instanceof AuthenticatedAgentTool authenticatedTool) {
            return authenticatedTool.getName();
        }

        throw new IllegalStateException(
                "Unsupported tool type: " + tool.getClass().getName());
    }

    private String getDescription(Object tool) {
        if (tool instanceof AgentTool publicTool) {
            return publicTool.getDescription();
        }

        if (tool instanceof AuthenticatedAgentTool authenticatedTool) {
            return authenticatedTool.getDescription();
        }

        throw new IllegalStateException(
                "Unsupported tool type: " + tool.getClass().getName());
    }
}