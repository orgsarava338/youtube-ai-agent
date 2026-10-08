package com.youtubeagent.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.core.LLMToolCall;
import com.youtubeagent.ai.core.LLMToolDefinition;
import com.youtubeagent.config.OpenRouterProperties;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("openrouter")
public class OpenRouterProvider implements LLMProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenRouterProvider(OpenRouterProperties properties, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderId() {
        return "openrouter";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {

        OpenRouterRequest requestBody = new OpenRouterRequest(
                request.model(),
                convertMessages(request.messages()),
                convertTools(request.tools()),
                false);

        OpenRouterResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(OpenRouterResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().getFirst().message() == null) {
            throw new IllegalStateException("OpenRouter returned an empty response.");
        }

        OpenRouterMessage message = response.choices().getFirst().message();
        String content = message.content();

        List<LLMToolCall> toolCalls = parseToolCalls(message.toolCalls());

        if ((content == null || content.isBlank()) && toolCalls.isEmpty()) {
            throw new IllegalStateException("OpenRouter returned an empty response");
        }

        return new LLMResponse(content, getProviderId(), request.model(), toolCalls);
    }

    private List<LLMToolCall> parseToolCalls(List<OpenRouterToolCall> rawToolCalls) {
        if (rawToolCalls == null || rawToolCalls.isEmpty()) {
            return List.of();
        }

        List<LLMToolCall> toolCalls = new ArrayList<>();

        for (OpenRouterToolCall rawToolCall : rawToolCalls) {

            if (rawToolCall == null || rawToolCall.function() == null) {
                continue;
            }

            String argumentsJson = rawToolCall.function().arguments();

            Map<String, Object> arguments;

            try {
                arguments = objectMapper.readValue(
                        argumentsJson,
                        new TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception e) {
                throw new IllegalStateException("Invalid tool arguments returned by OpenRouter", e);
            }

            toolCalls.add(new LLMToolCall(
                    rawToolCall.id(),
                    rawToolCall.function().name(),
                    arguments));
        }

        return toolCalls;
    }

    private List<OpenRouterMessage> convertMessages(List<LLMMessage> messages) {

        return messages.stream()
                .map(message -> {
                    List<OpenRouterToolCall> toolCalls = message.toolCalls()
                            .stream()
                            .map(toolCall -> new OpenRouterToolCall(
                                    toolCall.id(),
                                    "function",
                                    new OpenRouterFunction(toolCall.name(), writeArguments(toolCall.arguments()))))
                            .toList();

                    return new OpenRouterMessage(
                            message.role(),
                            message.content(),
                            toolCalls.isEmpty() ? null : toolCalls,
                            message.toolCallId());
                })
                .toList();
    }

    private String writeArguments(Map<String, Object> arguments) {
        try {
            return objectMapper.writeValueAsString(arguments == null ? Map.of() : arguments);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize tool arguments", e);
        }
    }

    private List<OpenRouterTool> convertTools(List<LLMToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return List.of();
        }

        return tools.stream()
                .map(tool -> new OpenRouterTool(
                        "function",
                        new OpenRouterFunctionDefinition(
                                tool.name(),
                                tool.description(),
                                tool.parameters())))
                .toList();
    }

    private record OpenRouterRequest(
            String model,
            List<OpenRouterMessage> messages,
                    List<OpenRouterTool> tools,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            OpenRouterMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterMessage(
            String role,
            String content,
            @JsonProperty("tool_calls") List<OpenRouterToolCall> toolCalls,
            @JsonProperty("tool_call_id") String toolCallId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterToolCall(
            String id,
            String type,
            OpenRouterFunction function) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterFunction(
            String name,
            String arguments) {
    }

    private record OpenRouterTool(
            String type,
            OpenRouterFunctionDefinition function) {
    }

    private record OpenRouterFunctionDefinition(
            String name,
            String description,
            Map<String, Object> parameters) {
    }
}