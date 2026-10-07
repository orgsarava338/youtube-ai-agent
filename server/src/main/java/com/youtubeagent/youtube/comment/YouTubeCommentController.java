package com.youtubeagent.youtube.comment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube")
public class YouTubeCommentController {

    private final YouTubeCommentService commentService;

    public YouTubeCommentController(YouTubeCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments")
    public List<YouTubeComment> getVideoComments(
            @RequestParam String videoId,
            @RequestParam(defaultValue = "10") int maxResults) {

        return commentService.getVideoComments(videoId, maxResults);
    }
}