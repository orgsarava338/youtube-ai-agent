package com.youtubeagent.youtube.channel;

import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class YouTubeChannelService {

    private final YouTubeOAuthService oauthService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubeChannelService(YouTubeOAuthService oauthService, ObjectMapper objectMapper) {
        this.oauthService = oauthService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public YouTubeChannel getMyChannel() {
        var token = oauthService.getValidToken();

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/channels")
                        .queryParam("part", "snippet,statistics")
                        .queryParam("mine", "true")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("YouTube API returned an empty response");
        }

        return parseChannel(response);
    }

    private YouTubeChannel parseChannel(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            if (items == null || !items.isArray() || items.isEmpty()) {
                throw new IllegalStateException("No YouTube channel found for the connected account");
            }

            JsonNode channel = items.get(0);

            String id = textValue(channel, "id");
            JsonNode snippet = channel.get("snippet");
            JsonNode statistics = channel.get("statistics");

            return new YouTubeChannel(
                    id,
                    textValue(snippet, "title"),
                    textValue(snippet, "description"),
                    textValue(snippet, "customUrl"),
                    thumbnailUrl(snippet),
                    longValue(statistics, "subscriberCount"),
                    longValue(statistics, "videoCount"),
                    longValue(statistics, "viewCount"));
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse YouTube channel response", exception);
        }
    }

    private String thumbnailUrl(JsonNode snippet) {
        if (snippet == null) {
            return null;
        }

        JsonNode thumbnails = snippet.get("thumbnails");
        if (thumbnails == null) {
            return null;
        }

        JsonNode high = thumbnails.get("high");
        if (high != null && high.get("url") != null) {
            return textValue(high, "url");
        }

        JsonNode medium = thumbnails.get("medium");
        if (medium != null && medium.get("url") != null) {
            return textValue(medium, "url");
        }

        JsonNode defaultThumbnail = thumbnails.get("default");
        if (defaultThumbnail != null && defaultThumbnail.get("url") != null) {
            return textValue(defaultThumbnail, "url");
        }

        return null;
    }

    private String textValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return null;
        }

        return node.get(field).stringValue();
    }

    private long longValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return 0;
        }

        return node.get(field).asLong();
    }
}