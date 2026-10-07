package com.youtubeagent.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "youtube")
public record YouTubeProperties(
        GoogleOAuth googleOAuth) {

    public record GoogleOAuth(
            String clientId,
            String clientSecret,
            String redirectUri) {
    }
}