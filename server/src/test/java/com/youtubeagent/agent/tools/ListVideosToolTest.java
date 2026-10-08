package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.video.YouTubeVideoService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListVideosToolTest {

    @Test
    void shouldUseDefaultMaxResults() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        ObjectMapper objectMapper = new ObjectMapper();

        when(service.getMyVideos(10))
                .thenReturn(List.of());

        ListVideosTool tool = new ListVideosTool(
                service,
                objectMapper);

        Object result = tool.execute(Map.of());

        assertEquals("[]", result);

        verify(service).getMyVideos(10);
    }

    @Test
    void shouldUseProvidedMaxResults() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        ObjectMapper objectMapper = new ObjectMapper();

        when(service.getMyVideos(5))
                .thenReturn(List.of());

        ListVideosTool tool = new ListVideosTool(
                service,
                objectMapper);

        Object result = tool.execute(
                Map.of("maxResults", 5));

        assertEquals("[]", result);

        verify(service).getMyVideos(5);
    }

    @Test
    void shouldSerializeVideos() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        ObjectMapper objectMapper = new ObjectMapper();

        YouTubeVideoSummary video = new YouTubeVideoSummary(
                "q4FcTKZVBfQ",
                "Listen if you can",
                "",
                Instant.parse("2023-08-22T09:12:38Z"),
                "thumbnail.jpg",
                "UC123",
                "Saravanan Lakshmanan");

        when(service.getMyVideos(10)).thenReturn(List.of(video));

        ListVideosTool tool = new ListVideosTool(
                service,
                objectMapper);

        String result = tool.execute(Map.of()).toString();

        assertTrue(result.contains("q4FcTKZVBfQ"));
        assertTrue(result.contains("Listen if you can"));

        verify(service).getMyVideos(10);
    }
}