package com.youtubeagent.youtube.video;

import com.youtubeagent.youtube.YouTubeJsonUtils;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeVideoService {

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com";

    private final YouTubeOAuthService oauthService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubeVideoService(YouTubeOAuthService oauthService, ObjectMapper objectMapper) {
        this.oauthService = oauthService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(YOUTUBE_API_BASE_URL).build();
    }

    public List<YouTubeVideo> getMyVideos(int maxResults) {

        if (maxResults < 1 || maxResults > 50) {
            throw new IllegalArgumentException("maxResults must be between 1 and 50");
        }

        var token = oauthService.getValidToken();

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/search")
                        .queryParam("part", "snippet")
                        .queryParam("forMine", "true")
                        .queryParam("type", "video")
                        .queryParam("order", "date")
                        .queryParam("maxResults", maxResults)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parseVideoList(response);
    }

    public YouTubeVideo getVideo(String videoId) {

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId is required");
        }

        var token = oauthService.getValidToken();

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/videos")
                        .queryParam("part", "snippet,contentDetails,statistics")
                        .queryParam("id", videoId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parseVideo(response, videoId);
    }

    private List<YouTubeVideo> parseVideoList(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");
            List<YouTubeVideo> videos = new ArrayList<>();

            if (items == null || !items.isArray()) {
                return videos;
            }

            for (JsonNode item : items) {
                videos.add(parseSearchVideoItem(item));
            }

            return videos;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube videos", e);
        }
    }

    private YouTubeVideo parseVideo(String response, String videoId) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            if (items == null || !items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("Video not found: " + videoId);
            }

            return parseVideoItem(items.get(0));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube video: " + videoId, e);
        }
    }

    private YouTubeVideo parseVideoItem(JsonNode item) {

        JsonNode snippet = item.get("snippet");
        JsonNode contentDetails = item.get("contentDetails");
        JsonNode statistics = item.get("statistics");

        return new YouTubeVideo(
                YouTubeJsonUtils.textValue(item, "id"),
                YouTubeJsonUtils.textValue(snippet, "title"),
                YouTubeJsonUtils.textValue(snippet, "description"),
                YouTubeJsonUtils.parseInstant(YouTubeJsonUtils.textValue(snippet, "publishedAt")),
                YouTubeJsonUtils.thumbnailUrl(snippet),
                YouTubeJsonUtils.textValue(snippet, "channelId"),
                YouTubeJsonUtils.textValue(snippet, "channelTitle"),
                YouTubeJsonUtils.textValue(contentDetails, "duration"),
                YouTubeJsonUtils.longValue(statistics, "viewCount"),
                YouTubeJsonUtils.longValue(statistics, "likeCount"),
                YouTubeJsonUtils.longValue(statistics, "commentCount"));
    }

    private YouTubeVideo parseSearchVideoItem(JsonNode item) {

        JsonNode id = item.get("id");
        JsonNode snippet = item.get("snippet");

        return new YouTubeVideo(
                YouTubeJsonUtils.textValue(id, "videoId"),
                YouTubeJsonUtils.textValue(snippet, "title"),
                YouTubeJsonUtils.textValue(snippet, "description"),
                YouTubeJsonUtils.parseInstant(YouTubeJsonUtils.textValue(snippet, "publishedAt")),
                YouTubeJsonUtils.thumbnailUrl(snippet),
                YouTubeJsonUtils.textValue(snippet, "channelId"),
                YouTubeJsonUtils.textValue(snippet, "channelTitle"),
                null, // duration
                0, // viewCount
                0, // likeCount
                0 // commentCount
        );
    }
}