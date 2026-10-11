package com.youtubeagent.youtube.oauth;

import com.youtubeagent.youtube.YouTubeProperties;
import com.youtubeagent.youtube.token.YouTubeTokenPersistenceService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubeOAuthServiceTest {
    private final YouTubeProperties properties = new YouTubeProperties(
            "client-id",
            new YouTubeProperties.GoogleOAuth("client-id", "client-secret", "https://example.test/callback"),
            new YouTubeProperties.TokenEncryption("unused"));
    private final YouTubeTokenPersistenceService persistence = mock(YouTubeTokenPersistenceService.class);
    private final YouTubeOAuthService service = new YouTubeOAuthService(properties, persistence);

    @Test
    void authorizationUrlIncludesOfflineAccessStateAndRequiredScopes() {
        String url = service.buildAuthorizationUrl("state-123");

        assertTrue(url.startsWith("https://accounts.google.com/o/oauth2/v2/auth?"));
        assertTrue(url.contains("client_id=client-id"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fexample.test%2Fcallback"));
        assertTrue(url.contains("access_type=offline"));
        assertTrue(url.contains("state=state-123"));
        assertTrue(url.contains("youtube.force-ssl"));
        assertTrue(url.contains("yt-analytics.readonly"));
    }

    @Test
    void rejectsBlankUserIdBeforeLoadingToken() {
        assertThrows(IllegalArgumentException.class, () -> service.getValidToken(" "));
        verifyNoInteractions(persistence);
    }

    @Test
    void reportsDisconnectedAccountWhenNoTokenIsSaved() {
        when(persistence.load("user-1")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getValidToken("user-1"));

        assertTrue(exception.getMessage().contains("YouTube account is not connected"));
        verify(persistence).load("user-1");
    }

    @Test
    void rejectsTokenWithoutRequiredScope() {
        when(persistence.load("user-1")).thenReturn(Optional.of(
                new YouTubeToken("access", "refresh", Long.MAX_VALUE, "openid profile", "Bearer")));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getValidToken("user-1"));

        assertTrue(exception.getMessage().contains("required scope"));
    }
}
