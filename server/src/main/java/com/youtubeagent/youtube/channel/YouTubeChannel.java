package com.youtubeagent.youtube.channel;

public record YouTubeChannel(
        String id,
        String title,
        String description,
        String customUrl,
        String thumbnailUrl,
        long subscriberCount,
        long videoCount,
        long viewCount,
        String uploadsPlaylistId) {
}