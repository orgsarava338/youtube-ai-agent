package com.youtubeagent.youtube.comment;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YouTubeCommentServiceTest {

    @Test
    void returnsParsedTopLevelComments() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        when(oauth.getValidToken()).thenReturn(token());
        YouTubeCommentService service = new YouTubeCommentService(oauth, new ObjectMapper());
        MockRestServiceServer server = RestClientTestSupport.install(service);
        server.expect(requestTo(containsString("/youtube/v3/commentThreads")))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"items":[{"snippet":{"totalReplyCount":2,"isPublic":true,
                        "topLevelComment":{"id":"comment-1","snippet":{"authorDisplayName":"Ada",
                        "authorChannelId":{"value":"author-1"},"textDisplay":"Nice video",
                        "likeCount":4,"publishedAt":"2024-01-02T03:04:05Z",
                        "updatedAt":"2024-01-03T03:04:05Z"}}}}]}
                        """, MediaType.APPLICATION_JSON));

        YouTubeComment result = service.getVideoComments("video-1", 20).getFirst();

        assertEquals("comment-1", result.commentId());
        assertEquals("Ada", result.authorName());
        assertEquals("author-1", result.authorChannelId());
        assertEquals("Nice video", result.text());
        assertEquals(4, result.likeCount());
        assertEquals(2, result.replyCount());
        assertEquals(true, result.isPublic());
        server.verify();
    }

    @Test
    void rejectsInvalidRequestBeforeFetchingToken() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        YouTubeCommentService service = new YouTubeCommentService(oauth, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> service.getVideoComments(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.getVideoComments("video-1", 101));
        verifyNoInteractions(oauth);
    }

    private static YouTubeToken token() {
        return new YouTubeToken("access-token", "refresh-token", Long.MAX_VALUE, "scope", "Bearer");
    }
}
