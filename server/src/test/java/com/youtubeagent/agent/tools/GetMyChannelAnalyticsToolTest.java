package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.analytics.YouTubeAnalytics;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetMyChannelAnalyticsToolTest {

    @Test
    void serializesAnalyticsForRequestedDateRange() {
        YouTubeAnalyticsService service = mock(YouTubeAnalyticsService.class);
        LocalDate start = LocalDate.parse("2024-01-01");
        LocalDate end = LocalDate.parse("2024-01-31");
        when(service.getMyChannelAnalytics(start, end)).thenReturn(new YouTubeAnalytics(start, end, 25, 60, 40, 4, 2, 1, 0));
        GetMyChannelAnalyticsTool tool = new GetMyChannelAnalyticsTool(service, new ObjectMapper());

        String result = tool.execute(Map.of("startDate", start.toString(), "endDate", end.toString())).toString();

        assertTrue(result.contains("\"views\":25"));
        assertTrue(result.contains("\"subscribersGained\":1"));
        verify(service).getMyChannelAnalytics(start, end);
    }

    @Test
    void rejectsMissingOrInvalidDatesWithoutCallingService() {
        YouTubeAnalyticsService service = mock(YouTubeAnalyticsService.class);
        GetMyChannelAnalyticsTool tool = new GetMyChannelAnalyticsTool(service, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("endDate", "2024-01-31")));
        assertThrows(IllegalArgumentException.class,
                () -> tool.execute(Map.of("startDate", "not-a-date", "endDate", "2024-01-31")));
        verifyNoInteractions(service);
    }

    @Test
    void rejectsStartDateAfterEndDateWithoutCallingService() {
        YouTubeAnalyticsService service = mock(YouTubeAnalyticsService.class);
        GetMyChannelAnalyticsTool tool = new GetMyChannelAnalyticsTool(service, new ObjectMapper());

        assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(Map.of(
                        "startDate", "2024-02-01",
                        "endDate", "2024-01-31")));

        verifyNoInteractions(service);
    }

}
