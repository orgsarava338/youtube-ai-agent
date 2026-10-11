package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YouTubeReadToolsTest {

    @Test
    void searchVideosTrimsQueryAndUsesDefaultOrRequestedLimit() {
        YouTubeVideoService service = mock(YouTubeVideoService.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(service.searchVideos("Tamil short film", 10)).thenReturn(List.of());
        when(service.searchVideos("Tamil short film", 4)).thenReturn(List.of());
        when(mapper.writeValueAsString(any())).thenReturn("[]");
        var tool = new SearchVideosTool(service, mapper);

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("query", "  ")));
        assertEquals("[]", tool.execute(Map.of("query", " Tamil short film ")));
        assertEquals("[]", tool.execute(Map.of("query", "Tamil short film", "maxResults", "4")));

        verify(service).searchVideos("Tamil short film", 10);
        verify(service).searchVideos("Tamil short film", 4);
    }

    @Test
    void playlistVideosRequiresPlaylistIdAndUsesDefaultLimit() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(service.getPlaylistVideos("PL-123", 10)).thenReturn(List.of());
        when(service.getPlaylistVideos("PL-123", 2)).thenReturn(List.of());
        when(mapper.writeValueAsString(any())).thenReturn("[]");
        var tool = new GetPlaylistVideosTool(service, mapper);

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("playlistId", " ")));
        assertEquals("[]", tool.execute(Map.of("playlistId", " PL-123 ")));
        assertEquals("[]", tool.execute(Map.of("playlistId", "PL-123", "maxResults", 2)));

        verify(service).getPlaylistVideos("PL-123", 10);
        verify(service).getPlaylistVideos("PL-123", 2);
    }

    @Test
    void currentTimeToolReturnsParseableTimestamp() {
        String timestamp = (String) new CurrentTimeTool().execute(Map.of());
        assertDoesNotThrow(() -> java.time.OffsetDateTime.parse(timestamp));
    }
}
