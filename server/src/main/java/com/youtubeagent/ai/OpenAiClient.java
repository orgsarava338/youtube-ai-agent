package com.youtubeagent.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.config.OpenAiProperties;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("openai")
public class OpenAiClient implements LLMProvider {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiClient(OpenAiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String generate(String prompt) {

        OpenAiRequest request = new OpenAiRequest(
                properties.model(),
                List.of(new Message("user", prompt)));

        OpenAiResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        return response.choices().get(0).message().content();
    }

    private record OpenAiRequest(
            String model,
            List<Message> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            Message message) {
    }

    private record Message(
            String role,
            String content) {
    }
}