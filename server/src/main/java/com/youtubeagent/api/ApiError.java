package com.youtubeagent.api;

import java.time.Instant;

public record ApiError(
        String type,
        String message,
        int status,
        Instant timestamp) {
}
