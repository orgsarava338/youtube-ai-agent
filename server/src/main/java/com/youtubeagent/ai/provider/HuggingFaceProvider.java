package com.youtubeagent.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.config.HuggingFaceProperties;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("huggingface")
public class HuggingFaceProvider implements LLMProvider {

    private final RestClient restClient;

    public HuggingFaceProvider(HuggingFaceProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getProviderId() {
        return "huggingface";
    }

    @Override
    public LLMResponse generate(LLMRequest request) {

        HuggingFaceRequest requestBody = new HuggingFaceRequest(request.model(), request.messages(), false);

        HuggingFaceResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .body(requestBody)
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

        String content = response.choices().get(0).message().content();
        return new LLMResponse(content, getProviderId(), request.model());
    }

    private record HuggingFaceRequest(
            String model,
            List<LLMMessage> messages,
            boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record HuggingFaceResponse(
            List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            LLMMessage message) {
    }
}