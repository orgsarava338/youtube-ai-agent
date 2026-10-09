package com.youtubeagent.ai.catalog.openrouter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterModel(
        String id,
        String name,
        @JsonProperty("context_length") Integer contextLength,
        Pricing pricing,
        @JsonProperty("supported_parameters") List<String> supportedParameters,
        @JsonProperty("architecture") Architecture architecture) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pricing(
            String prompt,
            String completion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Architecture(
            @JsonProperty("input_modalities") List<String> inputModalities,
            @JsonProperty("output_modalities") List<String> outputModalities) {
    }

    public boolean isFree() {
        return pricing != null
                && isZero(pricing.prompt())
                && isZero(pricing.completion());
    }

    private boolean isZero(String price) {
        if (price == null) {
            return false;
        }

        try {
            return Double.parseDouble(price) == 0.0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean supportsToolCalling() {
        return supportedParameters != null
                && supportedParameters.contains("tools")
                && supportedParameters.contains("tool_choice");
    }

    public boolean supportsTextInputAndOutput() {
        return architecture != null
                && architecture.inputModalities() != null
                && architecture.inputModalities().contains("text")
                && architecture.outputModalities() != null
                && architecture.outputModalities().contains("text");
    }
}