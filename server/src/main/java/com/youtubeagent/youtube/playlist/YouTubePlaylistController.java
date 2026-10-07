package com.youtubeagent.youtube.playlist;

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
    public List<YouTubePlaylist> getPlaylists(@RequestParam(defaultValue = "10") int maxResults) {
        return playlistService.getMyPlaylists(maxResults);
    }

    
}
