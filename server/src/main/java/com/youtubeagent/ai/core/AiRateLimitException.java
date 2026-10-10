package com.youtubeagent.ai.core;

public class AiRateLimitException extends RuntimeException {

    public AiRateLimitException() {
        super("AI model rate limit exceeded");
    }

    public AiRateLimitException(String message) {
        super(message);
    }

    public AiRateLimitException(Throwable cause) {
        super("AI model rate limit exceeded", cause);
    }
}