package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import com.youtubeagent.youtube.playlist.YouTubePlaylistVideo;
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

class GetPlaylistVideosToolTest {

    @Test
    void trimsIdPassesLimitAndSerializesPlaylistItems() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        when(service.getPlaylistVideos("playlist-1", 5)).thenReturn(List.of(
                new YouTubePlaylistVideo(new YouTubeVideoSummary("video-1", "Title", "Description",
                        Instant.EPOCH, null, "channel-1", "Channel"), 2)));
        GetPlaylistVideosTool tool = new GetPlaylistVideosTool(service, new ObjectMapper());

        String result = tool.execute(Map.of("playlistId", " playlist-1 ", "maxResults", 5)).toString();

        assertTrue(result.contains("video-1"));
        assertTrue(result.contains("\"position\":2"));
        verify(service).getPlaylistVideos("playlist-1", 5);
    }

    @Test
    void rejectsMissingOrBlankPlaylistId() {
        YouTubePlaylistService service = mock(YouTubePlaylistService.class);
        GetPlaylistVideosTool tool = new GetPlaylistVideosTool(service, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> tool.execute(null));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("playlistId", " ")));
        verifyNoInteractions(service);
    }
}
