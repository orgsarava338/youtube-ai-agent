package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.analytics.YouTubeAnalytics;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyVideoAnalyticsToolTest {
    private YouTubeAnalyticsService analyticsService;
    private GetMyVideoAnalyticsTool tool;

    @BeforeEach
    void setUp() {
        analyticsService = mock(YouTubeAnalyticsService.class);
        tool = new GetMyVideoAnalyticsTool(analyticsService, new ObjectMapper());
    }

    @Test
    void requestsAnalyticsForTheSpecifiedVideoAndRange() {
        LocalDate start = LocalDate.parse("2024-01-01");
        LocalDate end = LocalDate.parse("2024-01-31");
        when(analyticsService.getMyVideoAnalytics("user-123", "video-123", start, end))
                .thenReturn(new YouTubeAnalytics(start, end, 20, 60, 15, 2, 1, 1, 0));

        Object result = tool.execute(new ToolExecutionContext("user-123"),
                Map.of("videoId", " video-123 ", "startDate", "2024-01-01", "endDate", "2024-01-31"));

        assertTrue(result.toString().contains("\"views\":20"));
        verify(analyticsService).getMyVideoAnalytics("user-123", "video-123", start, end);
    }

    @Test
    void rejectsBlankVideoId() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"),
                        Map.of("videoId", "  ", "startDate", "2024-01-01", "endDate", "2024-01-31")));
        verifyNoInteractions(analyticsService);
    }

    @Test
    void rejectsInvalidOrReversedDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"),
                        Map.of("videoId", "video-123", "startDate", "bad-date", "endDate", "2024-01-31")));
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"),
                        Map.of("videoId", "video-123", "startDate", "2024-02-01", "endDate", "2024-01-31")));
        verifyNoInteractions(analyticsService);
    }
}