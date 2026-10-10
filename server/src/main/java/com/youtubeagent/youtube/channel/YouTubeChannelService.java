package com.youtubeagent.youtube.channel;

import com.youtubeagent.youtube.YouTubeJsonUtils;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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

    public YouTubeChannel getMyChannel(String userId) {
        var token = oauthService.getValidToken(userId);

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/channels")
                        .queryParam("part", "snippet,statistics,contentDetails")
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

    public List<YouTubeChannel> getAllMyChannels(String userId) {
        var token = oauthService.getValidToken(userId);

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/channels")
                        .queryParam("part", "snippet,statistics,contentDetails")
                        .queryParam("mine", "true")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("YouTube API returned an empty response");
        }

        return parseAllChannels(response);
    }

    private YouTubeChannel parseChannel(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode channels = root.get("items");

            if (channels == null || !channels.isArray() || channels.isEmpty()) {
                throw new IllegalStateException("No YouTube channel found for the connected account");
            }

            JsonNode firstChannel = channels.get(0);
            return getChannel(firstChannel);

        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse YouTube channel response", exception);
        }
    }

    private List<YouTubeChannel> parseAllChannels(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode channels = root.get("items");

            if (channels == null || !channels.isArray() || channels.isEmpty()) {
                throw new IllegalStateException("No YouTube channel found for the connected account");
            }

            List<YouTubeChannel> allMyChannels = new ArrayList<>();

            for (JsonNode channel : channels) {
                allMyChannels.add(getChannel(channel));
            }

            return allMyChannels;

        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse YouTube channel response", exception);
        }
    }

    private YouTubeChannel getChannel(JsonNode channel) {
        String id = YouTubeJsonUtils.textValue(channel, "id");
        
        JsonNode snippet = channel.get("snippet");
        JsonNode statistics = channel.get("statistics");
        String uploadsPlaylistId = YouTubeJsonUtils.textValue(
                channel.path("contentDetails").path("relatedPlaylists"),
                "uploads");

        return new YouTubeChannel(
                        id,
                YouTubeJsonUtils.textValue(snippet, "title"),
                YouTubeJsonUtils.textValue(snippet, "description"),
                YouTubeJsonUtils.textValue(snippet, "customUrl"),
                YouTubeJsonUtils.thumbnailUrl(snippet),
                YouTubeJsonUtils.longValue(statistics, "subscriberCount"),
                YouTubeJsonUtils.longValue(statistics, "videoCount"),
                YouTubeJsonUtils.longValue(statistics, "viewCount"),
                uploadsPlaylistId);
    }
}