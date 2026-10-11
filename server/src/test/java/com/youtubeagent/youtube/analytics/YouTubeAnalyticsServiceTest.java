package com.youtubeagent.youtube.analytics;

import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubeAnalyticsServiceTest {
    private final YouTubeOAuthService oauthService = mock(YouTubeOAuthService.class);
    private final YouTubeAnalyticsService service = new YouTubeAnalyticsService(oauthService, new ObjectMapper());

    @Test
    void rejectsMissingStartDateBeforeCallingOAuth() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics("user-1", null, LocalDate.parse("2025-01-01")));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsMissingEndDateBeforeCallingOAuth() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics("user-1", LocalDate.parse("2025-01-01"), null));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsReversedChannelDateRangeBeforeCallingOAuth() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics("user-1", LocalDate.parse("2025-02-01"),
                        LocalDate.parse("2025-01-01")));
        verifyNoInteractions(oauthService);
    }

    @Test
    void rejectsInvalidVideoAnalyticsArgumentsBeforeCallingOAuth() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyVideoAnalytics("user-1", " ", LocalDate.parse("2025-01-01"),
                        LocalDate.parse("2025-01-02")));
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyVideoAnalytics("user-1", "video-1", LocalDate.parse("2025-02-01"),
                        LocalDate.parse("2025-01-01")));
        verifyNoInteractions(oauthService);
    }
}
