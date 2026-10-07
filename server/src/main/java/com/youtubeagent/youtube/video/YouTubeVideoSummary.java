package com.youtubeagent.youtube.video;

import java.time.Instant;

public record YouTubeVideoSummary(
        String videoId,
        String title,
        String description,
        Instant publishedAt,
        String thumbnailUrl,
        String channelId,
        String channelTitle) {
}