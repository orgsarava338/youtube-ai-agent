package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.playlist.YouTubePlaylist;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetMyPlaylistsToolTest {

    @Test
    void defaultsToTenResultsAndSerializesPlaylists() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        when(service.getMyPlaylists(10)).thenReturn(List.of(
                new YouTubePlaylist("playlist-1", "Favorites", "", null, "channel-1", "My channel", null, 2)));
        GetMyPlaylistsTool tool = new GetMyPlaylistsTool(service, new ObjectMapper());

        String result = tool.execute(Map.of()).toString();

        assertTrue(result.contains("playlist-1"));
        assertTrue(result.contains("Favorites"));
        verify(service).getMyPlaylists(10);
    }

    @Test
    void passesRequestedLimitAndReturnsAnEmptyList() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        when(service.getMyPlaylists(25)).thenReturn(List.of());
        GetMyPlaylistsTool tool = new GetMyPlaylistsTool(service, new ObjectMapper());

        String result = tool.execute(Map.of("maxResults", "25")).toString();

        assertEquals("[]", result);
        verify(service).getMyPlaylists(25);
    }

    @Test
    void rejectsNonNumericLimit() {
        GetMyPlaylistsTool tool = new GetMyPlaylistsTool(mock(YouTubePlaylistService.class), new ObjectMapper());

        assertThrows(NumberFormatException.class, () -> tool.execute(Map.of("maxResults", "many")));
    }
}
