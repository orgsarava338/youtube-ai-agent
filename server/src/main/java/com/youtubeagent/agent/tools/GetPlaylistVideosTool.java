package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import com.youtubeagent.youtube.playlist.YouTubePlaylistVideo;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetPlaylistVideosTool implements AgentTool {

    private final YouTubePlaylistService playlistService;
    private final ObjectMapper objectMapper;

    public GetPlaylistVideosTool(YouTubePlaylistService playlistService, ObjectMapper objectMapper) {
        this.playlistService = playlistService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_playlist_videos";
    }

    @Override
    public String getDescription() {
        return """
                Returns videos contained in a YouTube playlist.

                Required argument:
                - playlistId: the actual YouTube playlist ID.

                Optional argument:
                - maxResults: number of videos to return,
                  from 1 to 50. Default is 10.

                Each returned item includes:
                - video: video summary containing:
                  - videoId: the actual YouTube video ID
                  - title: video title
                  - description: video description
                  - publishedAt: publication timestamp
                  - thumbnailUrl: thumbnail URL
                  - channelId: video channel ID
                  - channelTitle: video channel name
                - position: video's position in the playlist.

                Important:
                - playlistId must be a real YouTube playlist ID.
                - Never invent, guess, or use placeholder playlist IDs.
                - If the user provides a playlist title instead of an ID,
                  first use list_playlists to find the actual playlistId.
                - The returned position represents the video's order
                  within the playlist.
                - This tool returns playlist video metadata only.
                - Detailed video statistics such as views, likes,
                  comments, and duration are not included here.
                - If detailed information about a video is needed,
                  use get_video with the returned videoId.

                Use this tool when the user asks for videos contained
                in a specific playlist or wants to analyze the contents
                of a playlist.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null || arguments.get("playlistId") == null) {
            throw new IllegalArgumentException("Missing required argument: playlistId");
        }

        String playlistId = arguments.get("playlistId").toString().trim();

        if (playlistId.isBlank()) {
            throw new IllegalArgumentException("playlistId must not be blank");
        }

        int maxResults = 10;

        if (arguments.get("maxResults") != null) {

            Object value = arguments.get("maxResults");

            if (value instanceof Number number) {
                maxResults = number.intValue();
            } else {
                maxResults = Integer.parseInt(value.toString());
            }
        }

        List<YouTubePlaylistVideo> videos = playlistService.getPlaylistVideos(playlistId, maxResults);

        try {
            return objectMapper.writeValueAsString(videos);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube playlist videos", e);
        }
    }
}