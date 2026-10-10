package com.youtubeagent.youtube.playlist;

import com.youtubeagent.youtube.YouTubeJsonUtils;
import com.youtubeagent.youtube.YouTubeProperties;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import com.youtubeagent.youtube.video.YouTubeVideoSummary;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubePlaylistService {

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com";

    private final YouTubeOAuthService oauthService;
    private final YouTubeProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubePlaylistService(YouTubeOAuthService oauthService, YouTubeProperties properties,
            ObjectMapper objectMapper) {
        this.oauthService = oauthService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(YOUTUBE_API_BASE_URL).build();
    }

    public List<YouTubePlaylist> getMyPlaylists(String userId, int maxResults) {

        if (maxResults < 1 || maxResults > 50) {
            throw new IllegalArgumentException("maxResults must be between 1 and 50");
        }

        var token = oauthService.getValidToken(userId);

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/playlists")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("mine", true)
                        .queryParam("maxResults", maxResults)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parsePlaylists(response);
    }

    public List<YouTubePlaylistVideo> getPlaylistVideos(String playlistId, int maxResults) {

        if (playlistId == null || playlistId.isBlank()) {
            throw new IllegalArgumentException("playlistId is required");
        }

        if (maxResults < 1 || maxResults > 50) {
            throw new IllegalArgumentException("maxResults must be between 1 and 50");
        }

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/playlistItems")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("playlistId", playlistId)
                        .queryParam("maxResults", maxResults)
                        .queryParam("key", properties.apiKey())
                        .build())
                .retrieve()
                .body(String.class);

        return parsePlaylistVideos(response);
    }

    private List<YouTubePlaylist> parsePlaylists(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            List<YouTubePlaylist> playlists = new ArrayList<>();

            if (items == null || !items.isArray()) {
                return playlists;
            }

            for (JsonNode item : items) {
                playlists.add(parsePlaylist(item));
            }

            return playlists;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse YouTube playlists",
                    e);
        }
    }

    private YouTubePlaylist parsePlaylist(JsonNode item) {

        JsonNode snippet = item.get("snippet");
        JsonNode contentDetails = item.get("contentDetails");

        return new YouTubePlaylist(
                YouTubeJsonUtils.textValue(item, "id"),
                YouTubeJsonUtils.textValue(snippet, "title"),
                YouTubeJsonUtils.textValue(snippet, "description"),
                YouTubeJsonUtils.thumbnailUrl(snippet),
                YouTubeJsonUtils.textValue(snippet, "channelId"),
                YouTubeJsonUtils.textValue(snippet, "channelTitle"),
                YouTubeJsonUtils.instantValue(snippet, "publishedAt"),
                YouTubeJsonUtils.longValue(contentDetails, "itemCount"));
    }

    private List<YouTubePlaylistVideo> parsePlaylistVideos(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            List<YouTubePlaylistVideo> videos = new ArrayList<>();

            if (items == null || !items.isArray()) {
                return videos;
            }

            for (JsonNode item : items) {
                videos.add(parsePlaylistVideo(item));
            }

            return videos;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube playlist videos", e);
        }
    }

    private YouTubePlaylistVideo parsePlaylistVideo(JsonNode item) {

        JsonNode snippet = item.get("snippet");
        JsonNode contentDetails = item.get("contentDetails");

        String videoId = YouTubeJsonUtils.textValue(contentDetails, "videoId");

        if (videoId == null || videoId.isBlank()) {
            JsonNode resourceId = snippet == null
                    ? null
                    : snippet.get("resourceId");

            videoId = YouTubeJsonUtils.textValue(resourceId, "videoId");
        }

        YouTubeVideoSummary videoSummary = new YouTubeVideoSummary(
                videoId,
                YouTubeJsonUtils.textValue(snippet, "title"),
                YouTubeJsonUtils.textValue(snippet, "description"),
                YouTubeJsonUtils.instantValue(snippet, "publishedAt"),
                YouTubeJsonUtils.thumbnailUrl(snippet),
                YouTubeJsonUtils.textValue(snippet, "channelId"),
                YouTubeJsonUtils.textValue(snippet, "channelTitle"));

        int position = (int) YouTubeJsonUtils.longValue(snippet, "position");

        return new YouTubePlaylistVideo(videoSummary, position);
    }
}