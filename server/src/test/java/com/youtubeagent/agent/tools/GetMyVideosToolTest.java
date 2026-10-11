package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyVideosToolTest {
    private YouTubeVideoService videoService;
    private GetMyVideosTool tool;

    @BeforeEach
    void setUp() {
        videoService = mock(YouTubeVideoService.class);
        tool = new GetMyVideosTool(videoService, new ObjectMapper());
    }

    @Test
    void defaultsToTenVideosWhenArgumentsAreAbsent() {
        when(videoService.getMyVideos("user-123", 10)).thenReturn(List.of());

        Object result = tool.execute(new ToolExecutionContext("user-123"), null);

        assertEquals("[]", result);
        verify(videoService).getMyVideos("user-123", 10);
    }

    @Test
    void passesRequestedLimitAndSerializesVideos() {
        when(videoService.getMyVideos("user-123", 1)).thenReturn(List.of(
                new YouTubeVideoSummary("video-1", "Latest video", "description",
                        Instant.parse("2024-01-01T00:00:00Z"), null, "channel-1", "Test Channel")));

        Object result = tool.execute(new ToolExecutionContext("user-123"), Map.of("maxResults", "1"));

        assertTrue(result.toString().contains("\"videoId\":\"video-1\""));
        assertTrue(result.toString().contains("\"title\":\"Latest video\""));
        verify(videoService).getMyVideos("user-123", 1);
    }

    @Test
    void rejectsNonNumericLimit() {
        assertThrows(NumberFormatException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"), Map.of("maxResults", "many")));
        verifyNoInteractions(videoService);
    }
}