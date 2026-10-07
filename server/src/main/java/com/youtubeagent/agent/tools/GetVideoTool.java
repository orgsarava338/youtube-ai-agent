package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.video.YouTubeVideo;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetVideoTool implements AgentTool {

    private final YouTubeVideoService videoService;
    private final ObjectMapper objectMapper;

    public GetVideoTool(YouTubeVideoService videoService, ObjectMapper objectMapper) {
        this.videoService = videoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_video";
    }

    @Override
    public String getDescription() {
        return """
                Returns detailed information about a YouTube video
                from the connected channel.

                Required argument:
                - videoId: the actual YouTube video ID string.

                Example:
                {
                  "videoId": "q4FcTKZVBfQ"
                }

                Important:
                - videoId must be a real YouTube video ID.
                - If the user provides a video ID, use it exactly.
                - If the user provides only a video title, first use list_videos
                  to find the video and obtain its actual videoId.
                - Never invent, guess, or use placeholder values for videoId.
                - Never use expressions or references such as
                  "{output_of_list_videos[0].videoId}".
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null || arguments.get("videoId") == null) {
            throw new IllegalArgumentException("Missing required argument: videoId");
        }

        String videoId = arguments.get("videoId").toString().trim();

        if (videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        YouTubeVideo video = videoService.getVideo(videoId);

        try {
            return objectMapper.writeValueAsString(video);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube video", e);
        }
    }
}