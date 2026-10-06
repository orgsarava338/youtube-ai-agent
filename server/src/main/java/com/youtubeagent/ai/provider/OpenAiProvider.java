package com.youtubeagent.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.OpenAiProperties;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("openai")
public class OpenAiProvider implements LLMProvider {

    private final RestClient restClient;

    public OpenAiProvider(OpenAiProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String getProviderId() {
        return "openai";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {

        OpenAiRequest requestBody = new OpenAiRequest(request.model(), request.messages());

        OpenAiResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().getFirst().message() == null
                || response.choices().getFirst().message().content() == null
                || response.choices().getFirst().message().content().isBlank()) {
            throw new IllegalStateException("OpenAI returned an empty response");
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

    private record OpenAiRequest(
            String model,
            List<LLMMessage> messages) {
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