package com.youtubeagent.ai.catalog.openrouter;

import com.youtubeagent.config.OpenRouterProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OpenRouterModelsClient {

    private final RestClient restClient;

    public OpenRouterModelsClient(OpenRouterProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + properties.apiKey())
                .build();
    }

    public List<OpenRouterModel> getModels() {
        OpenRouterModelsResponse response = restClient.get()
                .uri("/v1/models")
                .retrieve()
                .body(OpenRouterModelsResponse.class);

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "OpenRouter returned an empty model catalog.");
        }

        return response.data();
    }

    public List<OpenRouterModel> getFreeToolCallingModels() {
        return getModels().stream()
                .filter(model -> !"openrouter/free".equals(model.id()))
                .filter(model -> model.isFree())
                .filter(model -> model.supportsToolCalling())
                .filter(model -> model.supportsTextInputAndOutput())
                .toList();
    }
}