package com.youtubeagent.agent.tools;

import com.youtubeagent.youtube.video.YouTubeVideo;
import com.youtubeagent.youtube.video.YouTubeVideoService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetVideoToolTest {

    @Test
    void shouldReturnVideoInformation() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        ObjectMapper objectMapper = new ObjectMapper();

        YouTubeVideo video = new YouTubeVideo(
                "q4FcTKZVBfQ",
                "Listen if you can",
                "",
                Instant.parse("2023-08-22T09:12:38Z"),
                "thumbnail.jpg",
                "UC123",
                "Saravanan Lakshmanan",
                "PT1M1S",
                17,
                1,
                0);

        when(service.getVideo("q4FcTKZVBfQ"))
                .thenReturn(video);

        GetVideoTool tool = new GetVideoTool(
                service,
                objectMapper);

        String result = tool.execute(
                Map.of(
                        "videoId",
                        "q4FcTKZVBfQ"))
                .toString();

        assertTrue(result.contains("q4FcTKZVBfQ"));
        assertTrue(result.contains("Listen if you can"));
        assertTrue(result.contains("PT1M1S"));
        assertTrue(result.contains("17"));
        assertTrue(result.contains("1"));

        verify(service)
                .getVideo("q4FcTKZVBfQ");
    }

    @Test
    void shouldRejectMissingVideoId() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        GetVideoTool tool = new GetVideoTool(
                service,
                new ObjectMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(Map.of()));

        assertEquals(
                "Missing required argument: videoId",
                exception.getMessage());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectBlankVideoId() {

        YouTubeVideoService service = mock(YouTubeVideoService.class);

        GetVideoTool tool = new GetVideoTool(
                service,
                new ObjectMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(
                        Map.of("videoId", "   ")));

        assertEquals(
                "videoId must not be blank",
                exception.getMessage());

        verifyNoInteractions(service);
    }
}