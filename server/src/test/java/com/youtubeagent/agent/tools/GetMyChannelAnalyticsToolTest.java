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

class GetMyChannelAnalyticsToolTest {
    private YouTubeAnalyticsService analyticsService;
    private GetMyChannelAnalyticsTool tool;

    @BeforeEach
    void setUp() {
        analyticsService = mock(YouTubeAnalyticsService.class);
        tool = new GetMyChannelAnalyticsTool(analyticsService, new ObjectMapper());
    }

    @Test
    void requestsAnalyticsForTheProvidedInclusiveDateRange() {
        LocalDate start = LocalDate.parse("2024-01-01");
        LocalDate end = LocalDate.parse("2024-01-31");
        when(analyticsService.getMyChannelAnalytics("user-123", start, end))
                .thenReturn(new YouTubeAnalytics(start, end, 100, 250, 30, 8, 2, 4, 1));

        Object result = tool.execute(new ToolExecutionContext("user-123"),
                Map.of("startDate", "2024-01-01", "endDate", "2024-01-31"));

        assertTrue(result.toString().contains("\"views\":100"));
        assertTrue(result.toString().contains("\"subscribersGained\":4"));
        verify(analyticsService).getMyChannelAnalytics("user-123", start, end);
    }

    @Test
    void rejectsMissingDatesBeforeCallingService() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"), Map.of("startDate", "2024-01-01")));
        verifyNoInteractions(analyticsService);
    }

    @Test
    void rejectsInvalidDateFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"),
                        Map.of("startDate", "01-01-2024", "endDate", "2024-01-31")));
        verifyNoInteractions(analyticsService);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"),
                        Map.of("startDate", "2024-02-01", "endDate", "2024-01-31")));
        verifyNoInteractions(analyticsService);
    }
}