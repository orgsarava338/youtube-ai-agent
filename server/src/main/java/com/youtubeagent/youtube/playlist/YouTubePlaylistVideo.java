package com.youtubeagent.youtube.playlist;

import com.youtubeagent.youtube.video.YouTubeVideoSummary;

public record YouTubePlaylistVideo(
        YouTubeVideoSummary video,
        int position) {
}