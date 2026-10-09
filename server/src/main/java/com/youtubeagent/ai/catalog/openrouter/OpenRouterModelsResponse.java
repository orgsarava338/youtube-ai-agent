package com.youtubeagent.ai.catalog.openrouter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterModelsResponse(
        List<OpenRouterModel> data) {
}