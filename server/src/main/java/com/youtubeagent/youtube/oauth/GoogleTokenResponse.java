package com.youtubeagent.youtube.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("access_token") String accessToken,

        @JsonProperty("expires_in") long expiresIn,

        @JsonProperty("refresh_token") String refreshToken,

        @JsonProperty("scope") String scope,

        @JsonProperty("token_type") String tokenType) {
}