package com.youtubeagent.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.config.OpenRouterProperties;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("openrouter")
public class OpenRouterClient implements LLMProvider {

    private final RestClient restClient;
    private final OpenRouterProperties properties;

    public OpenRouterClient(OpenRouterProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generate(String prompt) {

        OpenRouterRequest request = new OpenRouterRequest(
                properties.model(),
                List.of(new Message("user", prompt)),
                false);

        OpenRouterResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .body(request)
                .retrieve()
                .body(OpenRouterResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {
            throw new IllegalStateException("OpenRouter returned an empty response");
        }

        return response.choices().get(0).message().content();
    }

    private record OpenRouterRequest(
            String model,
            List<Message> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(
            String role,
            String content) {
    }
}