package com.youtubeagent.youtube.channel;

import com.youtubeagent.youtube.YouTubeJsonUtils;
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

            String id = YouTubeJsonUtils.textValue(channel, "id");
            JsonNode snippet = channel.get("snippet");
            JsonNode statistics = channel.get("statistics");

            return new YouTubeChannel(
                    id,
                    YouTubeJsonUtils.textValue(snippet, "title"),
                    YouTubeJsonUtils.textValue(snippet, "description"),
                    YouTubeJsonUtils.textValue(snippet, "customUrl"),
                    YouTubeJsonUtils.thumbnailUrl(snippet),
                    YouTubeJsonUtils.longValue(statistics, "subscriberCount"),
                    YouTubeJsonUtils.longValue(statistics, "videoCount"),
                    YouTubeJsonUtils.longValue(statistics, "viewCount"));
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse YouTube channel response", exception);
        }
    }
}