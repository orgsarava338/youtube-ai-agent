package com.youtubeagent.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.config.HuggingFaceProperties;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("huggingface")
public class HuggingFaceClient implements LLMProvider {

    private final RestClient restClient;
    private final HuggingFaceProperties properties;

    public HuggingFaceClient(HuggingFaceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl()) // https://router.huggingface.co
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generate(String prompt) {

        HuggingFaceRequest request = new HuggingFaceRequest(
                properties.model(),
                List.of(new Message("user", prompt)),
                false);

        HuggingFaceResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .body(request)
                .retrieve()
                .body(HuggingFaceResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {
            throw new IllegalStateException("HuggingFace returned an empty response");
        }

        return response.choices().get(0).message().content();
    }

    private record HuggingFaceRequest(
            String model,
            List<Message> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record HuggingFaceResponse(
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