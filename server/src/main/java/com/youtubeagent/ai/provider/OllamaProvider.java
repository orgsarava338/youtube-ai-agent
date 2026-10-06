package com.youtubeagent.ai.provider;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.OllamaProperties;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("ollama")
public class OllamaProvider implements LLMProvider {

    private final RestClient restClient;

    public OllamaProvider(OllamaProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public String getProviderId() {
        return "ollama";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {

        OllamaGenerateRequest requestBody = new OllamaGenerateRequest(request.model(), request.messages(), false);

        OllamaGenerateResponse response = restClient.post()
                .uri("/api/generate")
                .body(requestBody)
                .retrieve()
                .body(OllamaGenerateResponse.class);

        if (response == null || response.response() == null) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return new LLMResponse(response.response(), getProviderId(), request.model());
    }

    private record OllamaGenerateRequest(
            String model,
            List<LLMMessage> messages,
            boolean stream) {
    }

    private record OllamaGenerateResponse(
            String response) {
    }
}