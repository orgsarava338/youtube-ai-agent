package com.youtubeagent.youtube.comment;

import java.time.Instant;

public record YouTubeComment(
        String commentId,
        String authorName,
        String authorChannelId,
        String authorProfileImageUrl,
        String text,
        Instant publishedAt,
        Instant updatedAt,
        long likeCount,
        long replyCount,
        boolean isPublic) {
}