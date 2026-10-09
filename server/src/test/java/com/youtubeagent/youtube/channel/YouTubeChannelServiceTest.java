package com.youtubeagent.youtube.channel;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.oauth.YouTubeToken;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;
import org.springframework.http.MediaType;

class YouTubeChannelServiceTest {

    @Test
    void loadsChannelAndStatistics() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        when(oauth.getValidToken()).thenReturn(token());
        YouTubeChannelService service = new YouTubeChannelService(oauth, new ObjectMapper());
        MockRestServiceServer server = RestClientTestSupport.install(service);
        server.expect(requestTo(containsString("/youtube/v3/channels")))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"items":[{"id":"channel-1","snippet":{"title":"My channel",
                        "description":"Description","customUrl":"@me",
                        "thumbnails":{"default":{"url":"thumb.jpg"}}},
                        "statistics":{"subscriberCount":"12","videoCount":"3","viewCount":"45"}}]}
                        """, MediaType.APPLICATION_JSON));

        YouTubeChannel result = service.getMyChannel();

        assertEquals("channel-1", result.id());
        assertEquals("My channel", result.title());
        assertEquals("@me", result.customUrl());
        assertEquals("thumb.jpg", result.thumbnailUrl());
        assertEquals(12, result.subscriberCount());
        assertEquals(3, result.videoCount());
        assertEquals(45, result.viewCount());
        server.verify();
    }

    private static YouTubeToken token() {
        return new YouTubeToken("access-token", "refresh-token", Long.MAX_VALUE, "scope", "Bearer");
    }
}
