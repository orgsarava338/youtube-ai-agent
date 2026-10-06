package com.youtubeagent.ai.provider;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.NvidiaProperties;

@Service("nvidia")
public class NvidiaProvider implements LLMProvider {

    private final RestClient restClient;

    public NvidiaProvider(NvidiaProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("provider-key", getProviderId())
                .build();
    }

    @Override
    public String getProviderId() {
        return "nvidia";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        NvidiaRequest requestBody = new NvidiaRequest(request.model(), request.messages(), false);

        NvidiaResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(NvidiaResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {
            throw new IllegalStateException("Bytez returned an empty response");
        }

        String content = response
                .choices()
                .getFirst()
                .message()
                .content();

        return new LLMResponse(
                content,
                getProviderId(),
                request.model());
    }

    private record NvidiaRequest(
            String model,
            List<LLMMessage> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NvidiaResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            LLMMessage message) {
    }

}
