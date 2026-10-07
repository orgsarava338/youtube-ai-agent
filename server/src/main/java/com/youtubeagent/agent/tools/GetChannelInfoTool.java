package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetChannelInfoTool implements AgentTool {

    private final YouTubeChannelService channelService;
    private final ObjectMapper objectMapper;

    public GetChannelInfoTool(YouTubeChannelService channelService, ObjectMapper objectMapper) {
        this.channelService = channelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_channel_info";
    }

    @Override
    public String getDescription() {
        return """
                Returns detailed information about the connected YouTube channel.

                No arguments are required.

                Returns:
                - id: the YouTube channel ID
                - title: the channel name
                - customUrl: the channel handle
                - description: the channel description
                - thumbnailUrl: the channel thumbnail URL
                - subscriberCount: current subscriber count
                - videoCount: total number of videos
                - viewCount: total channel views

                Example result:
                {
                  "id": "UC6uwApfNnEIf1Aj4nxFPDgg",
                  "title": "Saravanan Lakshmanan",
                  "customUrl": "@sarava338",
                  "description": "I'm not white and not black",
                  "subscriberCount": 45,
                  "videoCount": 35,
                  "viewCount": 5520
                }

                Use this tool when the user asks about their YouTube channel,
                channel name, handle, description, subscribers, video count,
                or total channel views.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        YouTubeChannel channel = channelService.getMyChannel();

        try {
            return objectMapper.writeValueAsString(channel);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube channel information", e);
        }
    }
}