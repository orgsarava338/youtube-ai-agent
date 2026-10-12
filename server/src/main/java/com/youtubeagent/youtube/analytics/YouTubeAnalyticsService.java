package com.youtubeagent.youtube.analytics;

import com.youtubeagent.youtube.YouTubeJsonUtils;
import com.youtubeagent.youtube.oauth.YouTubeOAuthService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeAnalyticsService {

    private static final String YOUTUBE_ANALYTICS_API_BASE_URL = "https://youtubeanalytics.googleapis.com";

    private static final String ANALYTICS_METRICS = "views,estimatedMinutesWatched,averageViewDuration,likes,comments,subscribersGained,subscribersLost";

    private final YouTubeOAuthService oauthService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubeAnalyticsService(YouTubeOAuthService oauthService, ObjectMapper objectMapper) {
        this.oauthService = oauthService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(YOUTUBE_ANALYTICS_API_BASE_URL).build();
    }

    public YouTubeAnalytics getMyChannelAnalytics(String userId, LocalDate startDate, LocalDate endDate) {

        validateDateRange(startDate, endDate);

        var token = oauthService.getValidAnalyticsToken(userId);

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/reports")
                        .queryParam("ids", "channel==MINE")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("metrics", ANALYTICS_METRICS)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parseAnalytics(response, startDate, endDate);
    }

    public YouTubeAnalytics getMyVideoAnalytics(String userId, String videoId, LocalDate startDate, LocalDate endDate) {

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId is required");
        }

        validateDateRange(startDate, endDate);

        var token = oauthService.getValidAnalyticsToken(userId);

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/reports")
                        .queryParam("ids", "channel==MINE")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("metrics", ANALYTICS_METRICS)
                        .queryParam("filters", "video==" + videoId.trim())
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parseAnalytics(response, startDate, endDate);
    }


    /**
     * Returns a ranked, multi-video analytics report in one API request.
     */
    public List<YouTubeVideoAnalytics> getMyTopVideoAnalytics(
            String userId, LocalDate startDate, LocalDate endDate, int maxResults) {

        validateDateRange(startDate, endDate);
        if (maxResults < 1 || maxResults > 50) {
            throw new IllegalArgumentException("maxResults must be between 1 and 50");
        }

        var token = oauthService.getValidAnalyticsToken(userId);
        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/reports")
                        .queryParam("ids", "channel==MINE")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("metrics", ANALYTICS_METRICS)
                        .queryParam("dimensions", "video")
                        .queryParam("sort", "-views")
                        .queryParam("maxResults", maxResults)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(String.class);

        return parseVideoAnalytics(response, startDate, endDate);
    }

    private List<YouTubeVideoAnalytics> parseVideoAnalytics(
            String response, LocalDate startDate, LocalDate endDate) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode rows = root.get("rows");
            List<YouTubeVideoAnalytics> results = new ArrayList<>();
            if (rows == null || !rows.isArray() || rows.isEmpty()) {
                return results;
            }

            for (JsonNode row : rows) {
                if (row == null || !row.isArray() || row.size() < 8) {
                    continue;
                }
                String videoId = row.get(0).asString();
                if (videoId == null || videoId.isBlank()) {
                    continue;
                }
                results.add(new YouTubeVideoAnalytics(
                        videoId, startDate, endDate,
                        YouTubeJsonUtils.longValue(row, 1),
                        YouTubeJsonUtils.longValue(row, 2),
                        YouTubeJsonUtils.longValue(row, 3),
                        YouTubeJsonUtils.longValue(row, 4),
                        YouTubeJsonUtils.longValue(row, 5),
                        YouTubeJsonUtils.longValue(row, 6),
                        YouTubeJsonUtils.longValue(row, 7)));
            }
            return results;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube video analytics response", e);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {

        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("endDate is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    private YouTubeAnalytics parseAnalytics(String response, LocalDate startDate, LocalDate endDate) {

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode rows = root.get("rows");

            if (rows == null || !rows.isArray() || rows.isEmpty()) {
                return new YouTubeAnalytics(startDate, endDate);
            }

            JsonNode row = rows.get(0);

            return new YouTubeAnalytics(
                    startDate,
                    endDate,
                    YouTubeJsonUtils.longValue(row, 0),
                    YouTubeJsonUtils.longValue(row, 1),
                    YouTubeJsonUtils.longValue(row, 2),
                    YouTubeJsonUtils.longValue(row, 3),
                    YouTubeJsonUtils.longValue(row, 4),
                    YouTubeJsonUtils.longValue(row, 5),
                    YouTubeJsonUtils.longValue(row, 6));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YouTube Analytics response", e);
        }
    }
}