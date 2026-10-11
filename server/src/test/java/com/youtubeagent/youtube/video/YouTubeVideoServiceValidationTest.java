package com.youtubeagent.youtube.video;

import com.youtubeagent.youtube.YouTubeProperties;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubeVideoServiceValidationTest {
    private final YouTubeOAuthService oauthService = mock(YouTubeOAuthService.class);
    private final YouTubeProperties properties = new YouTubeProperties("api-key", null, null);
    private final YouTubeVideoService service = new YouTubeVideoService(oauthService, properties, new ObjectMapper());

    @Test
    void rejectsInvalidOwnedVideoLimitsBeforeOAuth() {
        assertThrows(IllegalArgumentException.class, () -> service.getMyVideos("user-1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.getMyVideos("user-1", 51));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsMissingVideoIdBeforeMakingRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.getVideo(null));
        assertThrows(IllegalArgumentException.class, () -> service.getVideo("  "));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsInvalidSearchQueryAndLimitsBeforeMakingRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.searchVideos(null, 10));
        assertThrows(IllegalArgumentException.class, () -> service.searchVideos("  ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.searchVideos("cats", 0));
        assertThrows(IllegalArgumentException.class, () -> service.searchVideos("cats", 51));
        verifyNoInteractions(oauthService);
    }
}
