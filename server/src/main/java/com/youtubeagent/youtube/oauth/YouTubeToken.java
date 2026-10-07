package com.youtubeagent.youtube.oauth;

public record YouTubeToken(
        String accessToken,
        String refreshToken,
        long expiresAtEpochSeconds,
        String scope,
        String tokenType) {

    public boolean isExpired() {
        // Refresh slightly before the actual expiry.
        return expiresAtEpochSeconds <= (System.currentTimeMillis() / 1000) + 60;
    }
}