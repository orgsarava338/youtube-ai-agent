package com.youtubeagent.youtube.analytics;

import java.time.LocalDate;

public record YouTubeVideoAnalytics(
        String videoId,
        LocalDate startDate,
        LocalDate endDate,
        long views,
        long estimatedMinutesWatched,
        long averageViewDurationSeconds,
        long likes,
        long comments,
        long subscribersGained,
        long subscribersLost) {
}
