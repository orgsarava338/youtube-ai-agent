package com.youtubeagent.youtube.oauth;

import com.youtubeagent.youtube.YouTubeProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class YouTubeOAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String YOUTUBE_READONLY_SCOPE = "https://www.googleapis.com/auth/youtube.readonly";

    private final YouTubeProperties properties;
    private final RestClient restClient;

    public YouTubeOAuthService(YouTubeProperties properties) {
        this.properties = properties;
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
                .queryParam("scope", YOUTUBE_READONLY_SCOPE)
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
}