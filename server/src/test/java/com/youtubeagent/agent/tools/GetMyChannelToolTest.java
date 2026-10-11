package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyChannelToolTest {
    private YouTubeChannelService channelService;
    private GetMyChannelTool tool;

    @BeforeEach
    void setUp() {
        channelService = mock(YouTubeChannelService.class);
        tool = new GetMyChannelTool(channelService, new ObjectMapper());
    }

    @Test
    void returnsChannelDetailsForAuthenticatedUser() {
        when(channelService.getMyChannel("user-123")).thenReturn(
                new YouTubeChannel("channel-1", "Test Channel", "description", "@test",
                        "https://example.com/thumb.jpg", 42, 8, 1200, "uploads-1"));

        Object result = tool.execute(new ToolExecutionContext("user-123"), Map.of());

        assertInstanceOf(String.class, result);
        assertTrue(result.toString().contains("\"title\":\"Test Channel\""));
        assertTrue(result.toString().contains("\"subscriberCount\":42"));
        verify(channelService).getMyChannel("user-123");
    }

    @Test
    void propagatesServiceFailure() {
        when(channelService.getMyChannel("user-123")).thenThrow(new IllegalStateException("not connected"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> tool.execute(new ToolExecutionContext("user-123"), Map.of()));

        assertEquals("not connected", error.getMessage());
    }
}