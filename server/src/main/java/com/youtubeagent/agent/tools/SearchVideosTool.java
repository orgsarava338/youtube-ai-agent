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

                Required arguments:
                - query: search terms

                Optional arguments:
                - maxResults: maximum number of results, 1-50,
                  default 10

                Important:
                - This performs a general YouTube search.
                - Results are not limited to the connected user's channel.
                - Use this tool when the user wants to search YouTube
                  generally.
                - Do not use this tool when the user explicitly asks
                  about their own videos. Use list_videos instead.
                - Never invent or guess video IDs.
                - This is a read-only operation.
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