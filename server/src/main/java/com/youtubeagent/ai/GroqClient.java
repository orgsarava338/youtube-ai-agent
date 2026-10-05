package com.youtubeagent.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtubeagent.config.GroqProperties;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GroqClient implements LLMClient {

    private final RestClient restClient;
    private final GroqProperties properties;

    public GroqClient(GroqProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String generate(String prompt) {

        GroqChatRequest request = new GroqChatRequest(
                properties.model(),
                List.of(new Message("user", prompt)),
                new ResponseFormat("json_object"));

        GroqChatResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(GroqChatResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null) {
            throw new IllegalStateException("Groq returned an empty response");
        }

        return response.choices().get(0).message().content();
    }

    private record GroqChatRequest(
            String model,
            List<Message> messages,
            @JsonProperty("response_format") ResponseFormat responseFormat) {
    }

    private record ResponseFormat(String type) {
    }

    private record Message(
            String role,
            String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GroqChatResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            Message message) {
    }
}