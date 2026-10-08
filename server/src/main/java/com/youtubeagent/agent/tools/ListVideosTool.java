package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListVideosTool implements AgentTool {

    private final YouTubeVideoService videoService;
    private final ObjectMapper objectMapper;

    public ListVideosTool(YouTubeVideoService videoService, ObjectMapper objectMapper) {
        this.videoService = videoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "list_videos";
    }

    @Override
    public String getDescription() {
        return """
                Lists videos published on the connected YouTube channel.

                Optional arguments:
                - maxResults: maximum number of videos to return, 1-50,
                  default 10

                Important:
                - This tool returns videos belonging ONLY to the connected
                  YouTube channel.
                - Use this tool when the user refers to "my videos",
                  "my latest video", "my uploads", or similar phrases.
                - Use this tool when the user wants to find or filter
                  videos from their own channel.
                - Do not use search_videos for requests specifically
                  about the user's own videos.
                - This is a read-only operation.
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

        List<YouTubeVideoSummary> videos = videoService.getMyVideos(maxResults);

        try {
            return objectMapper.writeValueAsString(videos);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube videos", e);
        }
    }
}