package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AuthenticatedAgentTool;
import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetMyChannelTool implements AuthenticatedAgentTool {

    private final YouTubeChannelService channelService;
    private final ObjectMapper objectMapper;

    public GetMyChannelTool(YouTubeChannelService channelService, ObjectMapper objectMapper) {
        this.channelService = channelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_my_channel";
    }

    @Override
    public String getDescription() {
        return """
                Retrieves information about the authenticated user's own
                YouTube channel from the YouTube API.

                No arguments are required.

                Returns:
                - id: actual YouTube channel ID
                - title: actual channel name
                - description: channel description
                - customUrl: channel handle
                - thumbnailUrl: channel thumbnail URL
                - subscriberCount: subscriber count
                - videoCount: total video count
                - viewCount: total channel views

                Use this tool whenever the user asks about their own
                YouTube channel, including its name, handle, description,
                subscriber count, video count, or total views.

                IMPORTANT:
                - The result contains data from the authenticated account.
                - Use the returned values as the source of truth.
                - Never guess or invent channel details.
                - Do not substitute example data or information from
                  another YouTube channel.
                """;
    }

    @Override
    public Object execute(ToolExecutionContext executionContext, Map<String, Object> arguments) {
        YouTubeChannel channel = channelService.getMyChannel(executionContext.userId());

        try {
            return objectMapper.writeValueAsString(channel);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube channel information", e);
        }
    }
}