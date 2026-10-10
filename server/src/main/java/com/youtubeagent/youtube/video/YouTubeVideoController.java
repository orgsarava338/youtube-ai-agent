package com.youtubeagent.youtube.video;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube")
public class YouTubeVideoController {

    private final YouTubeVideoService videoService;

    public YouTubeVideoController(YouTubeVideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/videos")
    public List<YouTubeVideoSummary> getVideos(@RequestParam(defaultValue = "10") int maxResults,
            @AuthenticationPrincipal OidcUser user) {
        return videoService.getMyVideos(user.getSubject(), maxResults);
    }

    @GetMapping("/search")
    public List<YouTubeVideoSummary> searchVideos(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int maxResults) {
        return videoService.searchVideos(query, maxResults);
    }

    @GetMapping("/video")
    public YouTubeVideo getVideo(@RequestParam String videoId) {
        return videoService.getVideo(videoId);
    }
}