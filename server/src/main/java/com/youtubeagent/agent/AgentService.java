package com.youtubeagent.agent;

import com.youtubeagent.ai.LLMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AgentService {

    private static final int MAX_ITERATIONS = 5;

    private final LLMClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentService(LLMClient llmClient, ToolRegistry toolRegistry, ObjectMapper objectMapper) {

        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    public AgentResponse chat(String message) {

        List<String> history = new ArrayList<>();

        // Initial user request
        history.add("""
                User:
                %s
                """.formatted(message));

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {

            String prompt = buildPrompt(history);

            log.debug("Agent iteration {} prompt:\n{}", iteration + 1, prompt);

            String llmResponse = llmClient.generate(prompt);

            AgentResponse response = parseResponse(llmResponse);

            if ("final_answer".equals(response.type())) {
                return response;
            }

            if (!"tool_call".equals(response.type())) {
                throw new IllegalStateException(
                        "Unexpected agent response type: " + response.type());
            }

            // Preserve the LLM's tool request
            history.add("""
                    Assistant:
                    %s
                    """.formatted(llmResponse));

            AgentTool tool = findTool(response.tool());

            Map<String, Object> arguments = response.arguments() != null
                    ? response.arguments()
                    : Map.of();

            Object result = tool.execute(arguments);

            // Preserve the tool result
            history.add("""
                    Tool (%s):
                    %s
                    """.formatted(tool.getName(), result));
        }

        throw new IllegalStateException(
                "Agent exceeded maximum iterations: " + MAX_ITERATIONS);
    }

    private String buildPrompt(List<String> history) {

        return """
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

                If you can answer the user, respond with:
                {
                    "type": "final_answer",
                    "content": "your answer"
                }

                Do not use markdown.
                Do not add explanations outside the JSON.

                Conversation history:
                %s

                Based on the conversation history, decide what to do next.
                """.formatted(
                describeTools(),
                String.join("\n\n", history));
    }

    private AgentResponse parseResponse(String response) {

        String json = extractJson(response);

        try {
            return objectMapper.readValue(json, AgentResponse.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("LLM returned invalid agent JSON: " + response, e);
        }
    }

    private String extractJson(String raw) {

        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');

        return (start >= 0 && end > start)
                ? raw.substring(start, end + 1)
                : raw;
    }

    private String describeTools() {

        return toolRegistry.getTools()
                .stream()
                .map(tool -> "- %s: %s"
                        .formatted(
                                tool.getName(),
                                tool.getDescription()))
                .reduce("", (a, b) -> a + b + "\n");
    }

    private AgentTool findTool(String toolName) {

        return toolRegistry.getTools()
                .stream()
                .filter(tool -> tool.getName().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown tool: " + toolName));
    }
}