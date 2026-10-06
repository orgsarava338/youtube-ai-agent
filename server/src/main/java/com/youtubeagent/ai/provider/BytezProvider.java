package com.youtubeagent.ai.provider;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.BytezProperties;

@Service("bytez")
public class BytezProvider implements LLMProvider {

    private final RestClient restClient;

    public BytezProvider(BytezProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("provider-key", getProviderId())
                .build();
    }

    @Override
    public String getProviderId() {
        return "bytez";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        BytezRequest requestBody = new BytezRequest(request.model(), request.messages(), false);

        BytezResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(BytezResponse.class);

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

    private record BytezRequest(
            String model,
            List<LLMMessage> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BytezResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            LLMMessage message) {
    }

}
