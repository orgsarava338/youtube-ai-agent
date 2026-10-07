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
                Lists videos from the connected YouTube channel.

                Each returned video includes a videoId field containing
                the actual YouTube video ID.

                Example result:
                [
                  {
                    "videoId": "q4FcTKZVBfQ",
                    "title": "Listen if you can"
                  }
                ]

                Optional argument:
                - maxResults: number of videos to return, from 1 to 50.
                  Default is 10.

                Use this tool first when the user refers to a video by title
                but does not provide its video ID.
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