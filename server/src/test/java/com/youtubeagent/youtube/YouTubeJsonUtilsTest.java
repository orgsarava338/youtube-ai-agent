package com.youtubeagent.youtube;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeJsonUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReadTextValue() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "title": "Listen if you can"
                }
                """);

        assertEquals(
                "Listen if you can",
                YouTubeJsonUtils.textValue(node, "title"));
    }

    @Test
    void shouldReturnNullWhenTextFieldMissing() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "title": "Listen if you can"
                }
                """);

        assertNull(
                YouTubeJsonUtils.textValue(node, "description"));
    }

    @Test
    void shouldReadLongValue() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "viewCount": "5520"
                }
                """);

        assertEquals(
                5520,
                YouTubeJsonUtils.longValue(node, "viewCount"));
    }

    @Test
    void shouldReturnZeroWhenLongFieldMissing() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "viewCount": "5520"
                }
                """);

        assertEquals(
                0,
                YouTubeJsonUtils.longValue(node, "likeCount"));
    }

    @Test
    void shouldReadInstantValue() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "publishedAt": "2023-08-22T09:12:38Z"
                }
                """);

        assertEquals(
                Instant.parse("2023-08-22T09:12:38Z"),
                YouTubeJsonUtils.instantValue(node, "publishedAt"));
    }

    @Test
    void shouldPreferHighThumbnail() throws Exception {
        JsonNode snippet = objectMapper.readTree("""
                {
                  "thumbnails": {
                    "default": {
                      "url": "default.jpg"
                    },
                    "medium": {
                      "url": "medium.jpg"
                    },
                    "high": {
                      "url": "high.jpg"
                    }
                  }
                }
                """);

        assertEquals(
                "high.jpg",
                YouTubeJsonUtils.thumbnailUrl(snippet));
    }

    @Test
    void shouldFallbackToMediumThumbnail() throws Exception {
        JsonNode snippet = objectMapper.readTree("""
                {
                  "thumbnails": {
                    "default": {
                      "url": "default.jpg"
                    },
                    "medium": {
                      "url": "medium.jpg"
                    }
                  }
                }
                """);

        assertEquals(
                "medium.jpg",
                YouTubeJsonUtils.thumbnailUrl(snippet));
    }

    @Test
    void shouldFallbackToDefaultThumbnail() throws Exception {
        JsonNode snippet = objectMapper.readTree("""
                {
                  "thumbnails": {
                    "default": {
                      "url": "default.jpg"
                    }
                  }
                }
                """);

        assertEquals(
                "default.jpg",
                YouTubeJsonUtils.thumbnailUrl(snippet));
    }

    @Test
    void shouldReturnNullWhenThumbnailMissing() throws Exception {
        JsonNode snippet = objectMapper.readTree("""
                {
                  "title": "Listen if you can"
                }
                """);

        assertNull(
                YouTubeJsonUtils.thumbnailUrl(snippet));
    }
}