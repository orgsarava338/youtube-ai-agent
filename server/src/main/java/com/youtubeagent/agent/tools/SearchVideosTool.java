package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchVideosTool implements AgentTool {

    private final YouTubeVideoService videoService;
    private final ObjectMapper objectMapper;

    public SearchVideosTool(YouTubeVideoService videoService, ObjectMapper objectMapper) {
        this.videoService = videoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "search_videos";
    }

    @Override
    public String getDescription() {
        return """
                Searches YouTube for videos matching a search query.

                Required argument:
                - query: the text to search for.

                Optional argument:
                - maxResults: number of results to return, from 1 to 50.
                  Default is 10.

                Each returned video includes:
                - videoId: the actual YouTube video ID
                - title: video title
                - description: video description
                - publishedAt: publication timestamp
                - thumbnailUrl: video thumbnail URL
                - channelId: channel ID
                - channelTitle: channel name

                Important:
                - This searches YouTube, not only the connected channel.
                - Results contain real YouTube video IDs.
                - Never invent or guess a video ID.
                - Use this tool when the user wants to search YouTube
                  for videos matching a topic, title, keyword, or phrase.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null || arguments.get("query") == null) {
            throw new IllegalArgumentException("Missing required argument: query");
        }

        String query = arguments.get("query").toString().trim();

        if (query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
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

        List<YouTubeVideoSummary> videos = videoService.searchVideos(query, maxResults);

        try {
            return objectMapper.writeValueAsString(videos);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube search results", e);
        }
    }
}