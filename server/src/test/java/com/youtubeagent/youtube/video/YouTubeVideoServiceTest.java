package com.youtubeagent.youtube.video;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.oauth.YouTubeToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YouTubeVideoServiceTest {

    @Test
    void parsesSearchResultsAndDetailedVideo() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        when(oauth.getValidToken()).thenReturn(token());
        YouTubeVideoService service = new YouTubeVideoService(oauth, new ObjectMapper());
        MockRestServiceServer server = RestClientTestSupport.install(service);
        server.expect(requestTo(containsString("/youtube/v3/search")))
                .andRespond(withSuccess("""
                        {"items":[{"id":{"videoId":"video-1"},"snippet":{"title":"Result",
                        "description":"Summary","publishedAt":"2024-01-02T03:04:05Z",
                        "channelId":"channel-1","channelTitle":"My channel"}}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("/youtube/v3/videos")))
                .andRespond(withSuccess("""
                        {"items":[{"id":"video-1","snippet":{"title":"Detailed",
                        "description":"Full details","publishedAt":"2024-01-02T03:04:05Z",
                        "channelId":"channel-1","channelTitle":"My channel"},
                        "contentDetails":{"duration":"PT1M"},"statistics":{"viewCount":"25",
                        "likeCount":"4","commentCount":"2"}}]}
                        """, MediaType.APPLICATION_JSON));

        YouTubeVideoSummary summary = service.searchVideos("test", 5).getFirst();
        YouTubeVideo video = service.getVideo("video-1");

        assertEquals("video-1", summary.videoId());
        assertEquals("Result", summary.title());
        assertEquals("video-1", video.videoId());
        assertEquals("Detailed", video.title());
        assertEquals("PT1M", video.duration());
        assertEquals(25, video.viewCount());
        assertEquals(4, video.likeCount());
        assertEquals(2, video.commentCount());
        server.verify();
    }

    @Test
    void rejectsInvalidQueryAndResultLimitsBeforeFetchingToken() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        YouTubeVideoService service = new YouTubeVideoService(oauth, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> service.searchVideos(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.getMyVideos(51));
        assertThrows(IllegalArgumentException.class, () -> service.getVideo(""));
        verifyNoInteractions(oauth);
    }

    private static YouTubeToken token() {
        return new YouTubeToken("access-token", "refresh-token", Long.MAX_VALUE, "scope", "Bearer");
    }
}
