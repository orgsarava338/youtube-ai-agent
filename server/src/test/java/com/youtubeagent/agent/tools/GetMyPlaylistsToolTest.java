package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.playlist.YouTubePlaylist;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyPlaylistsToolTest {
    private YouTubePlaylistService playlistService;
    private GetMyPlaylistsTool tool;

    @BeforeEach
    void setUp() {
        playlistService = mock(YouTubePlaylistService.class);
        tool = new GetMyPlaylistsTool(playlistService, new ObjectMapper());
    }

    @Test
    void defaultsToTenPlaylists() {
        when(playlistService.getMyPlaylists("user-123", 10)).thenReturn(List.of());

        assertEquals("[]", tool.execute(new ToolExecutionContext("user-123"), null));
        verify(playlistService).getMyPlaylists("user-123", 10);
    }

    @Test
    void passesLimitAndSerializesPlaylistDetails() {
        when(playlistService.getMyPlaylists("user-123", 2)).thenReturn(List.of(
                new YouTubePlaylist("playlist-1", "Favorites", "description", null,
                        "channel-1", "Test Channel", Instant.parse("2024-01-01T00:00:00Z"), 3)));

        Object result = tool.execute(new ToolExecutionContext("user-123"), Map.of("maxResults", 2));

        assertTrue(result.toString().contains("\"playlistId\":\"playlist-1\""));
        assertTrue(result.toString().contains("\"title\":\"Favorites\""));
        verify(playlistService).getMyPlaylists("user-123", 2);
    }
}