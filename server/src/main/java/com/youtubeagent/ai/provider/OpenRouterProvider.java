package com.youtubeagent.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.OpenRouterProperties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service("openrouter")
public class OpenRouterProvider implements LLMProvider {

    private final RestClient restClient;

    public OpenRouterProvider(OpenRouterProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String getProviderId() {
        return "openrouter";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        log.info("Calling openrouter model: {}", request.model());

        OpenRouterRequest requestBody = new OpenRouterRequest(request.model(), request.messages(), false);

        OpenRouterResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(requestBody)
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

    private record OpenRouterRequest(
            String model,
            List<LLMMessage> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenRouterResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            LLMMessage message) {
    }
}