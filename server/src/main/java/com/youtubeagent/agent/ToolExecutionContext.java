package com.youtubeagent.agent;

public record ToolExecutionContext(String userId) {

    public boolean isAuthenticated() {
        return userId != null && !userId.isBlank();
    }

    public String requireUserId() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Authentication is required to execute this tool.");
        }

        return userId;
    }
}