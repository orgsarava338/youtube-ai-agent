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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SearchVideosToolTest {

    @Test
    void trimsQueryDefaultsLimitAndSerializesResults() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        when(service.searchVideos("piano", 10)).thenReturn(List.of(
                new YouTubeVideoSummary("video-1", "Piano lesson", "", Instant.EPOCH, null,
                        "channel-1", "Teacher")));
        SearchVideosTool tool = new SearchVideosTool(service, new ObjectMapper());

        String result = tool.execute(Map.of("query", " piano ")).toString();

        assertTrue(result.contains("video-1"));
        assertTrue(result.contains("Piano lesson"));
        verify(service).searchVideos("piano", 10);
    }

    @Test
    void rejectsMissingOrBlankQueryWithoutCallingService() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        SearchVideosTool tool = new SearchVideosTool(service, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("query", "  ")));
        verifyNoInteractions(service);
    }
}
