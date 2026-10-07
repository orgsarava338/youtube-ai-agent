package com.youtubeagent.youtube.video;

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
    public List<YouTubeVideoSummary> getVideos(@RequestParam(defaultValue = "10") int maxResults) {
        return videoService.getMyVideos(maxResults);
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