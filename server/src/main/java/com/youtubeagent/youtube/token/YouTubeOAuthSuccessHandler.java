
package com.youtubeagent.youtube.token;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.youtubeagent.config.AppProperties;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.oauth.YouTubeToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class YouTubeOAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private static final Set<String> REQUIRED_SCOPES = Set.of(
            "https://www.googleapis.com/auth/youtube.force-ssl",
            "https://www.googleapis.com/auth/yt-analytics.readonly");

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final YouTubeOAuthService oAuthService;
    private final AppProperties appProperties;

    public YouTubeOAuthSuccessHandler(
            OAuth2AuthorizedClientService authorizedClientService,
            YouTubeOAuthService oAuthService,
            AppProperties appProperties) {
        this.authorizedClientService = authorizedClientService;
        this.oAuthService = oAuthService;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        try {
            if (!(authentication.getPrincipal() instanceof OidcUser user)) {
                throw new IllegalStateException("OAuth login succeeded without an OIDC user");
            }

            String userId = user.getSubject();

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    GOOGLE_REGISTRATION_ID,
                    authentication.getName());

            if (client == null) {
                throw new IllegalStateException("Google authorized client was not found");
            }

            OAuth2AccessToken accessToken = client.getAccessToken();
            Set<String> scopes = accessToken.getScopes();

            if (scopes == null || !scopes.containsAll(REQUIRED_SCOPES)) {
                log.error("Required YouTube scopes missing for user {}. " + "Granted scopes: {}", userId, scopes);

                throw new IllegalStateException("Required YouTube permissions were not granted");
            }

            if (accessToken.getExpiresAt() == null) {
                throw new IllegalStateException("Google access token expiry is missing");
            }

            OAuth2RefreshToken refreshToken = client.getRefreshToken();

            if (refreshToken == null) {
                log.error(
                        "Google did not provide a refresh token for user {}. "
                                + "The YouTube account must be reconnected.",
                        userId);

                throw new IllegalStateException(
                        "Google did not provide a refresh token. "
                                + "Please reconnect the YouTube account.");
            }

            YouTubeToken token = new YouTubeToken(
                    accessToken.getTokenValue(),
                    refreshToken == null ? null : refreshToken.getTokenValue(),
                    accessToken.getExpiresAt().getEpochSecond(),
                    scopes.stream().sorted().collect(Collectors.joining(" ")),
                    accessToken.getTokenType() == null ? "Bearer" : accessToken.getTokenType().getValue());

            // Persist using the Google OIDC subject as the user ID.
            oAuthService.saveToken(userId, token);

            log.info("YouTube token saved successfully for user: {}, scopes: {}", userId, scopes);

        } catch (RuntimeException exception) {
            log.error("Failed to persist YouTube OAuth tokens after login", exception);

            // Do not expose exception details or tokens to the browser.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to connect the YouTube account");
            return;
        }

        response.sendRedirect(appProperties.frontendUrl());
    }
}
