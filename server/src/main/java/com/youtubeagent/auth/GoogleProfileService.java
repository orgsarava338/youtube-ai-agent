package com.youtubeagent.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.oauth.YouTubeToken;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GoogleProfileService {

    private static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private final YouTubeOAuthService youTubeOAuthService;
    private final RestClient restClient;

    public GoogleProfileService(YouTubeOAuthService youTubeOAuthService) {
        this.youTubeOAuthService = youTubeOAuthService;
        this.restClient = RestClient.builder().build();
    }

    public GoogleProfile getProfile(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank");
        }

        // Reuse existing token storage and refresh logic.
        YouTubeToken token = youTubeOAuthService.getValidToken(userId);

        try {
            GoogleUserInfo googleUser = restClient.get()
                    .uri(USER_INFO_URL)
                    .headers(headers -> headers.setBearerAuth(token.accessToken()))
                    .retrieve()
                    .body(GoogleUserInfo.class);

            if (googleUser == null || googleUser.id() == null) {
                throw new GoogleProfileUnavailableException(
                        "Google returned an empty user profile");
            }

            // Ensure the Google account matches the authenticated AssisTube user.
            if (!userId.equals(googleUser.id())) {
                throw new GoogleProfileUnavailableException(
                        "Google profile does not match the authenticated user");
            }

            return new GoogleProfile(
                    googleUser.id(),
                    googleUser.name(),
                    googleUser.email(),
                    googleUser.picture());

        } catch (RestClientResponseException ex) {
            throw new GoogleProfileUnavailableException(
                    "Unable to retrieve the Google profile. HTTP status: "
                            + ex.getStatusCode().value(),
                    ex);
        }
    }

    public record GoogleProfile(
            String id,
            String name,
            String email,
            String picture) {
    }

    private record GoogleUserInfo(
            @JsonProperty("sub") String id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("picture") String picture) {
    }

    public static class GoogleProfileUnavailableException
            extends RuntimeException {

        public GoogleProfileUnavailableException(String message) {
            super(message);
        }

        public GoogleProfileUnavailableException(
                String message, Throwable cause) {
            super(message, cause);
        }
    }
}