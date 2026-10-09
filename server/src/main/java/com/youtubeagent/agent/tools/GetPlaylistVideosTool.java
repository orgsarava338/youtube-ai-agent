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
            - playlistId: the exact YouTube playlist ID returned by
              get_my_playlists.

            Optional argument:
            - maxResults: number of videos to return, from 1 to 50.
              Default is 10.

            CRITICAL PLAYLIST ID RULES:
            - ALWAYS obtain the playlistId from the actual output of
              get_my_playlists when selecting a user's playlist.
            - Copy the playlistId character for character from that output.
            - Preserve every letter, digit, uppercase character, lowercase
              character, underscore, and hyphen exactly as provided.
            - Do not retype, reconstruct, shorten, normalize, modify,
              or guess any part of the playlistId.
            - Do not substitute a playlist title, channel ID, or video ID.
            - Before calling this tool, verify that the playlistId exactly
              matches the ID returned by get_my_playlists.
            - If the playlist ID is unavailable in the conversation or
              tool output, call get_my_playlists again instead of guessing.
            - Never reuse a playlist ID from memory or a previous request
              when the current request requires a different playlist.

            Each returned item includes:
            - video: video summary containing videoId, title, description,
              publishedAt, thumbnailUrl, channelId, and channelTitle.
            - position: video's position within the playlist.

            This tool returns playlist video metadata only.
            For detailed video statistics such as views, likes, comments,
            and duration, use get_video with the returned videoId.

            Use this tool when the user asks for videos contained in
            a specific YouTube playlist.
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