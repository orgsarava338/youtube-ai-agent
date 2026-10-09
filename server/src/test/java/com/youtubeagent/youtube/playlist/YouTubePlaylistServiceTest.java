package com.youtubeagent.youtube.playlist;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YouTubePlaylistServiceTest {

    @Test
    void parsesPlaylistsAndPlaylistVideos() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        when(oauth.getValidToken()).thenReturn(token());
        YouTubePlaylistService service = new YouTubePlaylistService(oauth, new ObjectMapper());
        MockRestServiceServer server = RestClientTestSupport.install(service);
        server.expect(requestTo(containsString("/youtube/v3/playlists")))
                .andRespond(withSuccess("""
                        {"items":[{"id":"playlist-1","snippet":{"title":"Favorites",
                        "description":"Selected videos","channelId":"channel-1",
                        "channelTitle":"My channel","publishedAt":"2024-01-02T03:04:05Z"},
                        "contentDetails":{"itemCount":1}}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("/youtube/v3/playlistItems")))
                .andRespond(withSuccess("""
                        {"items":[{"snippet":{"title":"Video title","description":"Details",
                        "publishedAt":"2024-01-02T03:04:05Z","channelId":"channel-1",
                        "channelTitle":"My channel","position":0,
                        "resourceId":{"videoId":"video-1"}},
                        "contentDetails":{}}]}
                        """, MediaType.APPLICATION_JSON));

        YouTubePlaylist playlist = service.getMyPlaylists(10).getFirst();
        YouTubePlaylistVideo playlistVideo = service.getPlaylistVideos("playlist-1", 10).getFirst();

        assertEquals("playlist-1", playlist.playlistId());
        assertEquals("Favorites", playlist.title());
        assertEquals(1, playlist.videoCount());
        assertEquals("video-1", playlistVideo.video().videoId());
        assertEquals("Video title", playlistVideo.video().title());
        assertEquals(0, playlistVideo.position());
        server.verify();
    }

    @Test
    void rejectsInvalidLimitsAndMissingPlaylistId() {
        YouTubeOAuthService oauth = mock(YouTubeOAuthService.class);
        YouTubePlaylistService service = new YouTubePlaylistService(oauth, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> service.getMyPlaylists(0));
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.getPlaylistVideos("playlist-1", 51));
        verifyNoInteractions(oauth);
    }

    private static YouTubeToken token() {
        return new YouTubeToken("access-token", "refresh-token", Long.MAX_VALUE, "scope", "Bearer");
    }
}
