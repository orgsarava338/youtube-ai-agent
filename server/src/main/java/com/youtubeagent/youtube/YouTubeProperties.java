package com.youtubeagent.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "youtube")
public record YouTubeProperties(
        String apiKey,
        GoogleOAuth googleOAuth,
        TokenEncryption tokenEncryption) {

    public record GoogleOAuth(
            String clientId,
            String clientSecret,
            String redirectUri) {
    }

    public record TokenEncryption(
            String encryptionKey) {
    }
}