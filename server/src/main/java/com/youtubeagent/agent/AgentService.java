package com.youtubeagent.agent;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.youtubeagent.ai.LLMClient;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final LLMClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentService(LLMClient llmClient, ToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    public AgentResponse chat(String message) {

        String prompt = """
                You are an AI agent.

                You have access to the following tools:

                %s

                You MUST respond using ONLY valid JSON.

                If you need to use a tool, respond with:

                {
                  "type": "tool_call",
                  "tool": "tool_name",
                  "arguments": {}
                }

                If you can answer without using a tool, respond with:

                {
                  "type": "final_answer",
                  "content": "your answer"
                }

                Do not use markdown.
                Do not add explanations outside the JSON.

                User request:
                %s
                """.formatted(describeTools(), message);

        String llmResponse = llmClient.generate(prompt);

        AgentResponse agentResponse = parseResponse(llmResponse);

        if ("tool_call".equals(agentResponse.type())) {
            return executeToolAndContinue(message, prompt, agentResponse);
        }

        return agentResponse;
    }

    private AgentResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response, AgentResponse.class);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "LLM returned invalid agent JSON: " + response,
                    e
            );
        }
    }

    private String describeTools() {
        return toolRegistry.getTools()
                .stream()
                .map(tool -> "- %s: %s"
                        .formatted(tool.getName(), tool.getDescription()))
                .reduce("", (a, b) -> a + b + "\n");
    }

    private AgentResponse executeTool(AgentResponse agentResponse) {

        AgentTool tool = toolRegistry.getTools()
                .stream()
                .filter(t -> t.getName().equals(agentResponse.tool()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown tool: " + agentResponse.tool()
                ));

        Object result = tool.execute(
                agentResponse.arguments() != null
                        ? agentResponse.arguments()
                        : java.util.Map.of()
        );

        return new AgentResponse(
                "tool_result",
                tool.getName(),
                java.util.Map.of(),
                result.toString()
        );
    }

    private AgentResponse executeToolAndContinue(String userMessage, String originalPrompt, AgentResponse toolCall) {
        AgentTool tool = toolRegistry.getTools()
                .stream()
                .filter(t -> t.getName().equals(toolCall.tool()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown tool: " + toolCall.tool()
                ));
            
        Object result = tool.execute(
                toolCall.arguments() != null
                        ? toolCall.arguments()
                        : java.util.Map.of()
        );
    
        String followUpPrompt = """
                %s
    
                The user requested:
                %s
    
                You requested the following tool:
    
                {
                  "type": "tool_call",
                  "tool": "%s",
                  "arguments": %s
                }
    
                The tool has now been executed.
    
                Tool result:
                %s
    
                Now provide the final answer to the user.
    
                You MUST respond using ONLY valid JSON:
    
                {
                  "type": "final_answer",
                  "content": "your answer"
                }
    
                Do not call another tool.
                Do not use markdown.
                Do not add explanations outside the JSON.
                """.formatted(
                        originalPrompt,
                        userMessage,
                        tool.getName(),
                        objectMapper.valueToTree(toolCall.arguments()),
                        result
                );
            
        String finalLlmResponse = llmClient.generate(followUpPrompt);
            
        return parseResponse(finalLlmResponse);
    }
}