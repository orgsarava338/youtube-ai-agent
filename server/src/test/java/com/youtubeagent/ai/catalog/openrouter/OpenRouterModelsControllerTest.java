package com.youtubeagent.ai.catalog.openrouter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenRouterModelsControllerTest {

    @Test
    void ranksCandidatesByContextLengthThenId() {
        OpenRouterModelsClient client = mock(OpenRouterModelsClient.class);
        when(client.getFreeToolCallingModels()).thenReturn(List.of(
                model("z-small", 131_072), model("a-large", 1_000_000), model("b-medium", 262_144)));
        OpenRouterModelsController controller = new OpenRouterModelsController(client);

        List<OpenRouterModelsController.ModelCandidate> result = controller.getRankedModels();

        assertEquals(List.of("a-large", "b-medium", "z-small"),
                result.stream().map(OpenRouterModelsController.ModelCandidate::id).toList());
        assertEquals(List.of(40, 30, 20), result.stream()
                .map(OpenRouterModelsController.ModelCandidate::score).toList());
    }

    private static OpenRouterModel model(String id, int contextLength) {
        return new OpenRouterModel(id, id, contextLength,
                new OpenRouterModel.Pricing("0", "0"),
                List.of("tools", "tool_choice"),
                new OpenRouterModel.Architecture(List.of("text"), List.of("text")));
    }
}
