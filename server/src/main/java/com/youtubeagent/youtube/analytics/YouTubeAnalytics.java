package com.youtubeagent.youtube.analytics;

import java.time.LocalDate;

public record YouTubeAnalytics(
        LocalDate startDate,
        LocalDate endDate,
        long views,
        long estimatedMinutesWatched,
        long averageViewDurationSeconds,
        long likes,
        long comments,
        long subscribersGained,
        long subscribersLost) {

    public YouTubeAnalytics(LocalDate startDate, LocalDate endDate) {
        this(startDate, endDate, 0, 0, 0, 0, 0, 0, 0);
    }
}