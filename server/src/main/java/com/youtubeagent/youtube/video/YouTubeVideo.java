package com.youtubeagent.youtube.video;

import java.time.Instant;

public record YouTubeVideo(
        String videoId,
        String title,
        String description,
        Instant publishedAt,
        String thumbnailUrl,
        String channelId,
        String channelTitle,
        String duration,
        long viewCount,
        long likeCount,
        long commentCount) {
}