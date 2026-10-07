package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.channel.YouTubeChannel;
import com.youtubeagent.youtube.channel.YouTubeChannelService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetChannelInfoToolTest {

    @Test
    void shouldReturnChannelInformation() {

        YouTubeChannelService service = mock(YouTubeChannelService.class);

        ObjectMapper objectMapper = new ObjectMapper();

        YouTubeChannel channel = new YouTubeChannel(
                "UC123",
                "Saravanan Lakshmanan",
                "Channel description",
                "@sarava338",
                "thumbnail.jpg",
                45,
                35,
                5520);

        when(service.getMyChannel())
                .thenReturn(channel);

        GetChannelInfoTool tool = new GetChannelInfoTool(
                service,
                objectMapper);

        Object result = tool.execute(Map.of());

        assertNotNull(result);

        String json = result.toString();

        assertTrue(json.contains("UC123"));
        assertTrue(json.contains("Saravanan Lakshmanan"));
        assertTrue(json.contains("@sarava338"));
        assertTrue(json.contains("45"));
        assertTrue(json.contains("35"));
        assertTrue(json.contains("5520"));

        verify(service).getMyChannel();
    }
}