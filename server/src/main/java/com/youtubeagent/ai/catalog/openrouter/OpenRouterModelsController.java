
package com.youtubeagent.ai.catalog.openrouter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/models/openrouter")
public class OpenRouterModelsController {

    private final OpenRouterModelsClient modelsClient;

    public OpenRouterModelsController(OpenRouterModelsClient modelsClient) {
        this.modelsClient = modelsClient;
    }

    @GetMapping("/free-tool-calling")
    public List<OpenRouterModel> getFreeToolCallingModels() {
        return modelsClient.getFreeToolCallingModels();
    }

    @GetMapping("/ranked")
    public List<ModelCandidate> getRankedModels() {
        return modelsClient.getFreeToolCallingModels().stream()
                .map(ModelCandidate::from)
                .sorted(
                        Comparator.comparingInt(ModelCandidate::score).reversed()
                                .thenComparing(ModelCandidate::id))
                .toList();
    }

    public record ModelCandidate(
            String id,
            String name,
            Integer contextLength,
            String promptPrice,
            String completionPrice,
            boolean free,
            boolean toolCalling,
            boolean textInputOutput,
            int score) {

        static ModelCandidate from(OpenRouterModel model) {
            int score = 0;

            if (model.contextLength() != null) {
                if (model.contextLength() >= 1_000_000) {
                    score = 40;
                } else if (model.contextLength() >= 262_144) {
                    score = 30;
                } else if (model.contextLength() >= 131_072) {
                    score = 20;
                } else {
                    score = 10;
                }
            }

            return new ModelCandidate(
                    model.id(),
                    model.name(),
                    model.contextLength(),
                    model.pricing() == null ? null : model.pricing().prompt(),
                    model.pricing() == null ? null : model.pricing().completion(),
                    model.isFree(),
                    model.supportsToolCalling(),
                    model.supportsTextInputAndOutput(),
                    score);
        }
    }
}
