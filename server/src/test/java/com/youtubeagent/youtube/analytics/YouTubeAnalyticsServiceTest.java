package com.youtubeagent.youtube.analytics;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.oauth.YouTubeToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YouTubeAnalyticsServiceTest {

    @Test
    void mapsAnalyticsRowToMetrics() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        when(oauth.getValidAnalyticsToken()).thenReturn(
                new YouTubeToken("analytics-token", "refresh-token", Long.MAX_VALUE, "scope", "Bearer"));
        YouTubeAnalyticsService service = new YouTubeAnalyticsService(oauth, new ObjectMapper());
        MockRestServiceServer server = RestClientTestSupport.install(service);
        server.expect(requestTo(containsString("/v2/reports")))
                .andExpect(header("Authorization", "Bearer analytics-token"))
                .andRespond(withSuccess("""
                        {"columnHeaders":[{"name":"views"}],
                         "rows":[[100,250,30,8,3,2,1]]}
                        """, MediaType.APPLICATION_JSON));

        YouTubeAnalytics result = service.getMyChannelAnalytics(
                LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"));

        assertEquals(100, result.views());
        assertEquals(250, result.estimatedMinutesWatched());
        assertEquals(30, result.averageViewDurationSeconds());
        assertEquals(8, result.likes());
        assertEquals(3, result.comments());
        assertEquals(2, result.subscribersGained());
        assertEquals(1, result.subscribersLost());
        server.verify();
    }

    @Test
    void rejectsInvalidDateRangeBeforeFetchingToken() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        YouTubeAnalyticsService service = new YouTubeAnalyticsService(oauth, new ObjectMapper());

        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.getMyChannelAnalytics(LocalDate.parse("2024-02-01"), LocalDate.parse("2024-01-01")));
        verifyNoInteractions(oauth);
    }
}
