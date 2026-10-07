package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.playlist.YouTubePlaylist;
import com.youtubeagent.youtube.playlist.YouTubePlaylistService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListPlaylistsTool implements AgentTool {

    private final YouTubePlaylistService playlistService;
    private final ObjectMapper objectMapper;

    public ListPlaylistsTool(YouTubePlaylistService playlistService, ObjectMapper objectMapper) {
        this.playlistService = playlistService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "list_playlists";
    }

    @Override
    public String getDescription() {
        return """
                Returns playlists belonging to the connected YouTube channel.

                Optional argument:
                - maxResults: number of playlists to return,
                  from 1 to 50. Default is 10.

                Each returned playlist includes:
                - playlistId: the actual YouTube playlist ID
                - title: playlist title
                - description: playlist description
                - thumbnailUrl: playlist thumbnail
                - channelId: owning channel ID
                - channelTitle: owning channel name
                - publishedAt: playlist creation timestamp
                - videoCount: number of videos in the playlist

                Important:
                - These are playlists owned by the connected YouTube channel.
                - playlistId values are real YouTube playlist IDs.
                - Never invent or guess a playlist ID.
                - Use this tool when the user asks about their playlists
                  or wants to find a playlist by title or topic.

                If the user refers to a specific playlist by title,
                use this tool first to find its actual playlistId,
                then use get_playlist_videos with that playlistId.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        int maxResults = 10;

        if (arguments != null && arguments.get("maxResults") != null) {

            Object value = arguments.get("maxResults");

            if (value instanceof Number number) {
                maxResults = number.intValue();
            } else {
                maxResults = Integer.parseInt(value.toString());
            }
        }

        List<YouTubePlaylist> playlists = playlistService.getMyPlaylists(maxResults);

        try {
            return objectMapper.writeValueAsString(playlists);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube playlists", e);
        }
    }
}