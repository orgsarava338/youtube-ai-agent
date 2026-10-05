package com.youtubeagent.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.config.GeminiProperties;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiClient implements LLMClient {

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .build();
    }

    @Override
    public String generate(String prompt) {

        GeminiRequest request = new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                new GenerationConfig("application/json"));

        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", properties.model())
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            throw new IllegalStateException("Gemini returned an empty response");
        }

        return response.candidates().get(0).content().parts().get(0).text();
    }

    private record GeminiRequest(
            List<Content> contents,
            GenerationConfig generationConfig) {
    }

    private record GenerationConfig(String responseMimeType) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(
            List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Candidate(
            Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Content(
            List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Part(
            String text) {
    }
}