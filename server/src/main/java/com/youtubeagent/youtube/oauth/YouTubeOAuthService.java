package com.youtubeagent.youtube.oauth;

import com.youtubeagent.youtube.YouTubeProperties;
import com.youtubeagent.youtube.token.YouTubeTokenPersistenceService;

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
    private static final String YOUTUBE_ANALYTICS_SCOPE = "https://www.googleapis.com/auth/yt-analytics.readonly";
    private static final String YOUTUBE_SCOPES = YOUTUBE_ANALYTICS_SCOPE + " " + YOUTUBE_FORCE_SCOPE;

    private final YouTubeProperties properties;
    private final YouTubeTokenPersistenceService tokenPersistenceService;
    private final RestClient restClient;

    public YouTubeOAuthService(YouTubeProperties properties, YouTubeTokenPersistenceService tokenPersistenceService) {
        this.properties = properties;
        this.tokenPersistenceService = tokenPersistenceService;
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
                .queryParam("scope", YOUTUBE_SCOPES)
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

        return refreshedToken;
    }

    public YouTubeToken getValidToken(String userId) {
        return getValidToken(userId, YOUTUBE_FORCE_SCOPE);
    }

    public YouTubeToken getValidAnalyticsToken(String userId) {
        return getValidToken(userId, YOUTUBE_ANALYTICS_SCOPE);
    }

    private YouTubeToken getValidToken(String userId, String requiredScope) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank");
        }

        YouTubeToken token = tokenPersistenceService.load(userId)
                .orElseThrow(() -> new IllegalStateException("YouTube account is not connected for this user"));

        if (!hasRequiredScope(token, requiredScope)) {
            throw new IllegalStateException(
                    "YouTube OAuth token does not have the required scope: "
                            + requiredScope
                            + ". Please reconnect the YouTube account.");
        }

        if (!token.isExpired()) {
            return token;
        }


        YouTubeToken refreshedToken = refreshToken(token);
        refreshedToken = refreshTokenForUser(userId, refreshedToken);

        if (!hasRequiredScope(refreshedToken, requiredScope)) {
            throw new IllegalStateException(
                    "Refreshed YouTube OAuth token does not have the required "
                            + "scope: " + requiredScope
                            + ". Please reconnect the YouTube account.");
        }

        return refreshedToken;
    }

    private YouTubeToken refreshTokenForUser(String userId, YouTubeToken token) {

        if (token.refreshToken() == null || token.refreshToken().isBlank()) {
            throw new IllegalStateException(
                    "YouTube access token expired and no refresh token "
                            + "is available. Please reconnect the YouTube account.");
        }

        tokenPersistenceService.save(userId, token);

        return token;
    }

    public void saveToken(String userId, GoogleTokenResponse response) {

        long expiresAt = (System.currentTimeMillis() / 1000) + response.expiresIn();

        YouTubeToken token = new YouTubeToken(
                response.accessToken(),
                response.refreshToken(),
                expiresAt,
                response.scope(),
                response.tokenType());

        tokenPersistenceService.save(userId, token);
    }

    public void saveToken(String userId, YouTubeToken token) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank");
        }

        if (token == null) {
            throw new IllegalArgumentException("YouTube token must not be null");
        }

        tokenPersistenceService.save(userId, token);
    }

    private boolean hasRequiredScope(YouTubeToken token, String requiredScope) {
        if (token.scope() == null || token.scope().isBlank()) {
            return false;
        }

        return java.util.Arrays.stream(
                token.scope().split("\\s+"))
                .anyMatch(requiredScope::equals);
    }

}