package com.youtubeagent.ai.model;

import java.util.Set;

public record ModelDefinition(
        String id,
        String provider,
        boolean free,
        Set<ModelCapability> capabilities,
        int priority) {
}