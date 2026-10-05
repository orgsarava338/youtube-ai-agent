package com.youtubeagent.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.config.HuggingFaceProperties;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class HuggingFaceClient implements LLMClient {

    private final RestClient restClient;
    private final HuggingFaceProperties properties;

    public HuggingFaceClient(HuggingFaceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String generate(String prompt) {

        HuggingFaceRequest request = new HuggingFaceRequest(prompt);

        HuggingFaceResponse response = restClient.post()
                .uri("/models/{model}", properties.model())
                .body(request)
                .retrieve()
                .body(HuggingFaceResponse.class);

        if (response == null
                || response.generatedText() == null
                || response.generatedText().isEmpty()) {
            throw new IllegalStateException("HuggingFace returned an empty response");
        }

        return response.generatedText();
    }

    private record HuggingFaceRequest(
            String inputs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record HuggingFaceResponse(
            String generatedText) {
    }
}