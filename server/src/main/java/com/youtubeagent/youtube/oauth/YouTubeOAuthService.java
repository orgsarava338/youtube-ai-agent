package com.youtubeagent.youtube.oauth;

import com.youtubeagent.youtube.YouTubeProperties;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class YouTubeOAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String YOUTUBE_FORCE_SCOPE = "https://www.googleapis.com/auth/youtube.force-ssl";

    private final YouTubeProperties properties;
    private final EncryptedYouTubeTokenStore tokenStore;
    private final RestClient restClient;

    public YouTubeOAuthService(YouTubeProperties properties, EncryptedYouTubeTokenStore tokenStore) {
        this.properties = properties;
        this.tokenStore = tokenStore;
        this.restClient = RestClient.builder().build();
    }

    public String createState() {
        return UUID.randomUUID().toString();
    }

    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", properties.googleOAuth().clientId())
                .queryParam("redirect_uri", properties.googleOAuth().redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", YOUTUBE_FORCE_SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public GoogleTokenResponse exchangeCode(String code) {

        var formData = new LinkedMultiValueMap<String, String>();

        formData.add("code", code);
        formData.add("client_id", properties.googleOAuth().clientId());
        formData.add("client_secret", properties.googleOAuth().clientSecret());
        formData.add("redirect_uri", properties.googleOAuth().redirectUri());
        formData.add("grant_type", "authorization_code");

        GoogleTokenResponse response = restClient
                .post()
                .uri(GOOGLE_TOKEN_URL)
                .body(formData)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Google OAuth token exchange returned no access token");
        }

        return response;
    }

    public YouTubeToken getValidToken() {
        YouTubeToken token = tokenStore.load()
                .orElseThrow(() -> new IllegalStateException("YouTube account is not connected"));

        if (!hasRequiredScope(token)) {
            throw new IllegalStateException(
                    "YouTube OAuth token does not have the required scope: "
                            + YOUTUBE_FORCE_SCOPE
                            + ". Please reconnect the YouTube account.");
        }

        if (!token.isExpired()) {
            return token;
        }

        return refreshToken(token);
    }

    private YouTubeToken refreshToken(YouTubeToken currentToken) {
        if (currentToken.refreshToken() == null) {
            throw new IllegalStateException("YouTube access token expired and no refresh token is available");
        }

        var formData = new LinkedMultiValueMap<String, String>();

        formData.add("client_id", properties.googleOAuth().clientId());
        formData.add("client_secret", properties.googleOAuth().clientSecret());
        formData.add("refresh_token", currentToken.refreshToken());
        formData.add("grant_type", "refresh_token");

        GoogleTokenResponse response = restClient
                .post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Google token refresh returned no access token");
        }

        long expiresAt = (System.currentTimeMillis() / 1000) + response.expiresIn();

        YouTubeToken refreshedToken = new YouTubeToken(
                response.accessToken(),
                currentToken.refreshToken(),
                expiresAt,
                response.scope() != null ? response.scope() : currentToken.scope(),
                response.tokenType() != null ? response.tokenType() : currentToken.tokenType());

        tokenStore.save(refreshedToken);
        return refreshedToken;
    }

    public void saveToken(GoogleTokenResponse response) {

        long expiresAt = (System.currentTimeMillis() / 1000) + response.expiresIn();

        YouTubeToken token = new YouTubeToken(
                response.accessToken(),
                response.refreshToken(),
                expiresAt,
                response.scope(),
                response.tokenType());

        tokenStore.save(token);
    }

    private boolean hasRequiredScope(YouTubeToken token) {
        if (token.scope() == null || token.scope().isBlank()) {
            return false;
        }

        return java.util.Arrays.stream(
                token.scope().split("\\s+"))
                .anyMatch(YOUTUBE_FORCE_SCOPE::equals);
    }

    public YouTubeToken getStoredToken() {
        return tokenStore.load().get();
    }
}