package com.youtubeagent.ai.catalog.openrouter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenRouterModelTest {

    @Test
    void detectsFreePriceAndRequiredCapabilities() {
        OpenRouterModel model = new OpenRouterModel("model-a", "Model A", 128_000,
                new OpenRouterModel.Pricing("0", "0"),
                List.of("tools", "tool_choice"),
                new OpenRouterModel.Architecture(List.of("text"), List.of("text")));

        assertTrue(model.isFree());
        assertTrue(model.supportsToolCalling());
        assertTrue(model.supportsTextInputAndOutput());
    }

    @Test
    void rejectsNonzeroMalformedAndIncompleteCapabilities() {
        OpenRouterModel priced = new OpenRouterModel("model-b", "Model B", null,
                new OpenRouterModel.Pricing("0", "0.01"),
                List.of("tools"),
                new OpenRouterModel.Architecture(List.of("text"), List.of("image")));
        OpenRouterModel malformedPrice = new OpenRouterModel("model-c", "Model C", null,
                new OpenRouterModel.Pricing("free", "0"), null, null);

        assertFalse(priced.isFree());
        assertFalse(priced.supportsToolCalling());
        assertFalse(priced.supportsTextInputAndOutput());
        assertFalse(malformedPrice.isFree());
    }
}
