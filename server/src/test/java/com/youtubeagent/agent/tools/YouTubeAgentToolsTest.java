package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.analytics.YouTubeAnalytics;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import com.youtubeagent.youtube.playlist.YouTubePlaylist;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import com.youtubeagent.youtube.video.YouTubeVideo;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubeAgentToolsTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void getMyChannelUsesAuthenticatedUserAndSerializesResult() {
        YouTubeChannelService service = mock(YouTubeChannelService.class);
        when(service.getMyChannel("user-1")).thenReturn(new YouTubeChannel("channel-1", "My channel",
                "description", "@me", "thumb.jpg", 12, 34, 56, "uploads-1"));
        var tool = new GetMyChannelTool(service, mapper);
        String result = (String) tool.execute(new ToolExecutionContext("user-1"), Map.of());
        assertTrue(result.contains("My channel"));
        assertTrue(result.contains("channel-1"));
        verify(service).getMyChannel("user-1");
    }

    @Test
    void getMyVideosUsesDefaultAndExplicitLimits() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        when(service.getMyVideos("user-1", 10)).thenReturn(List.of());
        when(service.getMyVideos("user-1", 3)).thenReturn(List.of(new YouTubeVideoSummary("v1", "Latest", "",
                Instant.parse("2024-01-01T00:00:00Z"), null, "c1", "Channel")));
        var tool = new GetMyVideosTool(service, mapper);
        var context = new ToolExecutionContext("user-1");
        assertEquals("[]", tool.execute(context, null));
        assertTrue(((String) tool.execute(context, Map.of("maxResults", "3"))).contains("Latest"));
        verify(service).getMyVideos("user-1", 10);
        verify(service).getMyVideos("user-1", 3);
    }

    @Test
    void getMyPlaylistsUsesDefaultLimitAndSerializesPlaylists() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        when(service.getMyPlaylists("user-1", 10)).thenReturn(List.of(new YouTubePlaylist("pl1", "Favorites", "",
                null, "c1", "Channel", Instant.parse("2024-01-01T00:00:00Z"), 2)));
        var tool = new GetMyPlaylistsTool(service, mapper);
        assertTrue(((String) tool.execute(new ToolExecutionContext("user-1"), null)).contains("Favorites"));
        verify(service).getMyPlaylists("user-1", 10);
    }

    @Test
    void getVideoRejectsMissingOrBlankIdsAndSerializesDetails() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        when(service.getVideo("v1")).thenReturn(new YouTubeVideo("v1", "Details", "", null,
                null, "c1", "Channel", "PT1M", 100, 5, 2));
        var tool = new GetVideoTool(service, mapper);
        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("videoId", " ")));
        assertTrue(((String) tool.execute(Map.of("videoId", " v1 "))).contains("Details"));
        verify(service).getVideo("v1");
    }

    @Test
    void channelAnalyticsValidatesDatesAndPassesExactRange() {
        YouTubeAnalyticsService service = mock(YouTubeAnalyticsService.class);
        var analytics = new YouTubeAnalytics(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31"),
                10, 20, 30, 4, 5, 6, 7);
        when(service.getMyChannelAnalytics("user-1", analytics.startDate(), analytics.endDate())).thenReturn(analytics);
        var tool = new GetMyChannelAnalyticsTool(service, mapper);
        assertThrows(IllegalArgumentException.class, () -> tool.execute(new ToolExecutionContext("user-1"),
                Map.of("startDate", "bad-date", "endDate", "2025-01-31")));
        String result = (String) tool.execute(new ToolExecutionContext("user-1"),
                Map.of("startDate", "2025-01-01", "endDate", "2025-01-31"));
        assertTrue(result.contains("\"views\":10"));
        verify(service).getMyChannelAnalytics("user-1", analytics.startDate(), analytics.endDate());
    }

    @Test
    void videoAnalyticsRequiresVideoIdAndUsesActualId() {
        YouTubeAnalyticsService service = mock(YouTubeAnalyticsService.class);
        var analytics = new YouTubeAnalytics(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31"));
        when(service.getMyVideoAnalytics("user-1", "v1", analytics.startDate(), analytics.endDate()))
                .thenReturn(analytics);
        var tool = new GetMyVideoAnalyticsTool(service, mapper);
        assertThrows(IllegalArgumentException.class, () -> tool.execute(new ToolExecutionContext("user-1"),
                Map.of("startDate", "2025-01-01", "endDate", "2025-01-31")));
        String result = (String) tool.execute(new ToolExecutionContext("user-1"),
                Map.of("videoId", " v1 ", "startDate", "2025-01-01", "endDate", "2025-01-31"));
        assertTrue(result.contains("\"views\":0"));
        verify(service).getMyVideoAnalytics("user-1", "v1", analytics.startDate(), analytics.endDate());
    }
}
