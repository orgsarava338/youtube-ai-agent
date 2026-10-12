package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import com.youtubeagent.youtube.analytics.YouTubeVideoAnalytics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyTopVideoAnalyticsToolTest {
    private YouTubeAnalyticsService analyticsService;
    private GetMyTopVideoAnalyticsTool tool;

    @BeforeEach
    void setUp() {
        analyticsService = mock(YouTubeAnalyticsService.class);
        tool = new GetMyTopVideoAnalyticsTool(analyticsService, new ObjectMapper());
    }

    @Test
    void requestsRankedAnalyticsForSpecifiedDateRange() {
        LocalDate start = LocalDate.parse("2026-09-01");
        LocalDate end = LocalDate.parse("2026-09-30");
        when(analyticsService.getMyTopVideoAnalytics("user-123", start, end, 5))
                .thenReturn(List.of(new YouTubeVideoAnalytics("video-1", start, end, 100, 300, 30, 10, 2, 1, 0)));

        Object result = tool.execute(new ToolExecutionContext("user-123"), Map.of(
                "startDate", "2026-09-01", "endDate", "2026-09-30", "maxResults", 5));
        assertTrue(result.toString().contains("\"videoId\":\"video-1\""));
        assertTrue(result.toString().contains("\"views\":100"));
        verify(analyticsService).getMyTopVideoAnalytics("user-123", start, end, 5);
    }

    @Test
    void defaultsMaxResultsToTen() {
        LocalDate start = LocalDate.parse("2026-09-01");
        LocalDate end = LocalDate.parse("2026-09-30");
        when(analyticsService.getMyTopVideoAnalytics("user-123", start, end, 10)).thenReturn(List.of());
        tool.execute(new ToolExecutionContext("user-123"),
                Map.of("startDate", "2026-09-01", "endDate", "2026-09-30"));
        verify(analyticsService).getMyTopVideoAnalytics("user-123", start, end, 10);
    }

    @Test
    void rejectsInvalidRangesAndLimits() {
        ToolExecutionContext context = new ToolExecutionContext("user-123");
        assertThrows(IllegalArgumentException.class, () -> tool.execute(context,
                Map.of("startDate", "bad", "endDate", "2026-09-30")));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(context,
                Map.of("startDate", "2026-10-01", "endDate", "2026-09-30")));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(context,
                Map.of("startDate", "2026-09-01", "endDate", "2026-09-30", "maxResults", 51)));
        verifyNoInteractions(analyticsService);
    }
}
