package com.youtubeagent.agent;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.youtubeagent.ai.LLMClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AgentService {

    private final LLMClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    private final int MAX_ITERATIONS = 5;

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

        AgentResponse response = parseResponse(llmResponse);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {

            if ("final_answer".equals(response.type())) {
                return response;
            }

            if (!"tool_call".equals(response.type())) {
                throw new IllegalStateException("Unexpected agent response type: " + response.type());
            }

            response = executeToolAndContinue(message, prompt, response);
        }

        if ("final_answer".equals(response.type())) {
            return response;
        }

        throw new IllegalStateException("Agent exceeded maximum iterations: " + MAX_ITERATIONS);
    }

    private AgentResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response, AgentResponse.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("LLM returned invalid agent JSON: " + response, e);
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        return (start >= 0 && end > start) ? raw.substring(start, end + 1) : raw;
    }

    private String describeTools() {
        return toolRegistry.getTools()
                .stream()
                .map(tool -> "- %s: %s"
                        .formatted(tool.getName(), tool.getDescription()))
                .reduce("", (a, b) -> a + b + "\n");
    }

    private AgentResponse executeToolAndContinue(String userMessage, String originalPrompt, AgentResponse toolCall) {
        AgentTool tool = toolRegistry.getTools()
                .stream()
                .filter(t -> t.getName().equals(toolCall.tool()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolCall.tool()));

        Map<String, Object> arguments = toolCall.arguments() != null ? toolCall.arguments() : Map.of();
        Object result = tool.execute(arguments);

        String followUpPrompt = """
                You are an AI agent.
                The user's request was:
                %s
                You previously requested this tool:
                {
                  "type": "tool_call",
                  "tool": "%s",
                  "arguments": %s
                }
                The tool returned this result:
                %s
                Based on this result, decide what to do next.
                If you have enough information to answer the user, respond with ONLY:
                {
                  "type": "final_answer",
                  "content": "your answer"
                }
                If you need another tool, respond with ONLY:
                {
                  "type": "tool_call",
                  "tool": "tool_name",
                  "arguments": {}
                }
                Do not use markdown.
                Do not add explanations outside JSON.
                """.formatted(
                userMessage,
                tool.getName(),
                objectMapper.valueToTree(toolCall.arguments()),
                result);

        String finalLlmResponse = llmClient.generate(followUpPrompt);

        return parseResponse(finalLlmResponse);
    }
}