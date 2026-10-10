package com.youtubeagent.youtube.playlist;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube")
public class YouTubePlaylistController {

    private final YouTubePlaylistService playlistService;

    public YouTubePlaylistController(YouTubePlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/playlists")
    public List<YouTubePlaylist> getPlaylists(@RequestParam(defaultValue = "10") int maxResults, @AuthenticationPrincipal OidcUser user) {
        return playlistService.getMyPlaylists(user.getSubject(), maxResults);
    }

    
}
