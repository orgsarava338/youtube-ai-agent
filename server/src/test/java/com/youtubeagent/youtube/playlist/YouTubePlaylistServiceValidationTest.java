package com.youtubeagent.youtube.playlist;

import com.youtubeagent.youtube.YouTubeProperties;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubePlaylistServiceValidationTest {
    private final YouTubeOAuthService oauthService = mock(YouTubeOAuthService.class);
    private final YouTubeProperties properties = new YouTubeProperties("api-key", null, null);
    private final YouTubePlaylistService service = new YouTubePlaylistService(oauthService, properties, new ObjectMapper());

    @Test
    void rejectsInvalidPlaylistListLimitsBeforeOAuth() {
        assertThrows(IllegalArgumentException.class, () -> service.getMyPlaylists("user-1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.getMyPlaylists("user-1", 51));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsMissingPlaylistIdAndInvalidLimitsBeforeRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos(null, 10));
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos("  ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos("playlist-1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos("playlist-1", 51));
        verifyNoInteractions(oauthService);
    }
}
