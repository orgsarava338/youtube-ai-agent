package com.youtubeagent.ai.model;

import java.util.Set;

public record ModelRequirements(
        boolean freeOnly,
        Set<ModelCapability> requiredCapabilities) {
}