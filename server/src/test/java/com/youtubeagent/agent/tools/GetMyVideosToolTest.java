package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetMyVideosToolTest {

    @Test
    void defaultsToTenAndSerializesChannelVideos() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        when(service.getMyVideos(10)).thenReturn(List.of(
                new YouTubeVideoSummary("video-1", "My video", "Description", Instant.EPOCH, null,
                        "channel-1", "My channel")));
        GetMyVideosTool tool = new GetMyVideosTool(service, new ObjectMapper());

        String result = tool.execute(Map.of()).toString();

        assertTrue(result.contains("video-1"));
        assertTrue(result.contains("My video"));
        verify(service).getMyVideos(10);
    }

    @Test
    void passesRequestedLimitAndRejectsMalformedLimit() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        GetMyVideosTool tool = new GetMyVideosTool(service, new ObjectMapper());

        tool.execute(Map.of("maxResults", 15));

        verify(service).getMyVideos(15);
        assertThrows(NumberFormatException.class, () -> tool.execute(Map.of("maxResults", "invalid")));
    }
}
