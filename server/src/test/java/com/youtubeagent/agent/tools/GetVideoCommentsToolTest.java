package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.comment.YouTubeComment;
import com.youtubeagent.youtube.comment.YouTubeCommentService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetVideoCommentsToolTest {

    @Test
    void trimsVideoIdDefaultsLimitAndSerializesComments() {
        YouTubeCommentService service = mock(YouTubeCommentService.class);
        when(service.getVideoComments("video-1", 10)).thenReturn(List.of(
                new YouTubeComment("comment-1", "Ada", "author-1", null, "Nice", Instant.EPOCH,
                        Instant.EPOCH, 3, 0, true)));
        GetVideoCommentsTool tool = new GetVideoCommentsTool(service, new ObjectMapper());

        String result = tool.execute(Map.of("videoId", " video-1 ")).toString();

        assertTrue(result.contains("comment-1"));
        assertTrue(result.contains("Nice"));
        verify(service).getVideoComments("video-1", 10);
    }

    @Test
    void rejectsMissingOrBlankIdBeforeCallingService() {
        YouTubeCommentService service = mock(YouTubeCommentService.class);
        GetVideoCommentsTool tool = new GetVideoCommentsTool(service, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("videoId", " ")));
        verifyNoInteractions(service);
    }
}
