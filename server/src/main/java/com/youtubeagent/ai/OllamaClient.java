package com.youtubeagent.ai;

import com.youtubeagent.config.OllamaProperties;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("ollama")
public class OllamaClient implements LLMProvider {

    private final RestClient restClient;
    private final OllamaProperties properties;

    public OllamaClient(OllamaProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public String generate(String prompt) {

        OllamaGenerateRequest request = new OllamaGenerateRequest(
                properties.model(),
                prompt,
                false);

        OllamaGenerateResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaGenerateResponse.class);

        if (response == null || response.response() == null) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return response.response();
    }

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            boolean stream) {
    }

    private record OllamaGenerateResponse(
            String response) {
    }
}