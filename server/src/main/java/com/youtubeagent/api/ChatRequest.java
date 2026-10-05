package com.youtubeagent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        @Size(min = 1, max = 20000, message = "Message must be between 1 and 20000 characters")
        String message) {
}
