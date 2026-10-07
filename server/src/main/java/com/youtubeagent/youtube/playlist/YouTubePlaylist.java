package com.youtubeagent.youtube.playlist;

import java.time.Instant;

public record YouTubePlaylist(
        String playlistId,
        String title,
        String description,
        String thumbnailUrl,
        String channelId,
        String channelTitle,
        Instant publishedAt,
        long videoCount) {
}