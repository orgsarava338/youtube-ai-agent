package com.youtubeagent.youtube;

import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelController;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import com.youtubeagent.youtube.comment.YouTubeCommentService;
import com.youtubeagent.youtube.comment.YouTubeCommentController;
import com.youtubeagent.youtube.playlist.YouTubePlaylist;
import com.youtubeagent.youtube.playlist.YouTubePlaylistController;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import com.youtubeagent.youtube.video.YouTubeVideo;
import com.youtubeagent.youtube.video.YouTubeVideoController;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class YouTubeControllersTest {

    private YouTubeChannelService channelService;
    private YouTubeCommentService commentService;
    private YouTubePlaylistService playlistService;
    private YouTubeVideoService videoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        channelService = mock(YouTubeChannelService.class);
        commentService = mock(YouTubeCommentService.class);
        playlistService = mock(YouTubePlaylistService.class);
        videoService = mock(YouTubeVideoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new YouTubeChannelController(channelService),
                new YouTubeCommentController(commentService),
                new YouTubePlaylistController(playlistService),
                new YouTubeVideoController(videoService)).build();
    }

    @Test
    void servesChannelAndPlaylistEndpoints() throws Exception {
        when(channelService.getMyChannel()).thenReturn(
                new YouTubeChannel("channel-1", "My channel", "", "@me", null, 10, 2, 30, "hell"));
        when(playlistService.getMyPlaylists(10)).thenReturn(
                List.of(new YouTubePlaylist("playlist-1", "Favorites", "", null, "channel-1", "My channel",
                        Instant.EPOCH, 2)));

        mockMvc.perform(get("/api/v1/youtube/channel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("channel-1"));
        mockMvc.perform(get("/api/v1/youtube/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playlistId").value("playlist-1"));

        verify(channelService).getMyChannel();
        verify(playlistService).getMyPlaylists(10);
    }

    @Test
    void forwardsQueryParametersForVideoAndCommentEndpoints() throws Exception {
        when(videoService.getMyVideos(5)).thenReturn(List.of(
                new YouTubeVideoSummary("video-1", "Title", "", Instant.EPOCH, null, "channel-1", "Channel")));
        when(videoService.searchVideos("cats", 3)).thenReturn(List.of());
        when(videoService.getVideo("video-1")).thenReturn(
                new YouTubeVideo("video-1", "Title", "", Instant.EPOCH, null, "channel-1", "Channel",
                        "PT1M", 10, 1, 0));
        when(commentService.getVideoComments("video-1", 7)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/youtube/videos").param("maxResults", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].videoId").value("video-1"));
        mockMvc.perform(get("/api/v1/youtube/search").param("query", "cats").param("maxResults", "3"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/youtube/video").param("videoId", "video-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value("PT1M"));
        mockMvc.perform(get("/api/v1/youtube/comments").param("videoId", "video-1").param("maxResults", "7"))
                .andExpect(status().isOk());

        verify(videoService).getMyVideos(5);
        verify(videoService).searchVideos("cats", 3);
        verify(videoService).getVideo("video-1");
        verify(commentService).getVideoComments("video-1", 7);
    }
}
