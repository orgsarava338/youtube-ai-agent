package com.youtubeagent.youtube.comment;

import com.youtubeagent.youtube.YouTubeJsonUtils;
import com.youtubeagent.youtube.YouTubeProperties;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeCommentService {

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com";

    private  final YouTubeProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubeCommentService(YouTubeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(YOUTUBE_API_BASE_URL).build();
    }

    public List<YouTubeComment> getVideoComments(String videoId, int maxResults) {

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId is required");
        }

        if (maxResults < 1 || maxResults > 100) {
            throw new IllegalArgumentException("maxResults must be between 1 and 100");
        }

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/commentThreads")
                        .queryParam("part", "snippet")
                        .queryParam("videoId", videoId)
                        .queryParam("maxResults", maxResults)
                        .queryParam("order", "time")
                        .queryParam("key", properties.apiKey())
                        .build())
                .retrieve()
                .body(String.class);

        return parseComments(response);
    }

    private List<YouTubeComment> parseComments(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            List<YouTubeComment> comments = new ArrayList<>();

            if (items == null || !items.isArray()) {
                return comments;
            }

            for (JsonNode item : items) {
                comments.add(parseComment(item));
            }

            return comments;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube comments", e);
        }
    }

    private YouTubeComment parseComment(JsonNode item) {

        JsonNode snippet = item.get("snippet");
        JsonNode topLevelComment = snippet.get("topLevelComment");
        JsonNode commentSnippet = topLevelComment.get("snippet");

        return new YouTubeComment(
                YouTubeJsonUtils.textValue(topLevelComment, "id"),
                YouTubeJsonUtils.textValue(commentSnippet, "authorDisplayName"),
                extractAuthorChannelId(commentSnippet),
                YouTubeJsonUtils.textValue(commentSnippet, "authorProfileImageUrl"),
                YouTubeJsonUtils.textValue(commentSnippet, "textDisplay"),
                YouTubeJsonUtils.instantValue(commentSnippet, "publishedAt"),
                        YouTubeJsonUtils.instantValue(commentSnippet, "updatedAt"),
                YouTubeJsonUtils.longValue(commentSnippet, "likeCount"),
                YouTubeJsonUtils.longValue(snippet, "totalReplyCount"),
                YouTubeJsonUtils.booleanValue(snippet, "isPublic"));
    }

    private String extractAuthorChannelId(JsonNode commentSnippet) {
        if (commentSnippet == null) {
            return null;
        }

        JsonNode authorChannelId = commentSnippet.get("authorChannelId");

        if (authorChannelId == null || authorChannelId.isNull()) {
            return null;
        }

        if (authorChannelId.isObject()) {
            return YouTubeJsonUtils.textValue(authorChannelId, "value");
        }

        return authorChannelId.stringValue();
    }
}