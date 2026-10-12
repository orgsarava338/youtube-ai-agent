package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AuthenticatedAgentTool;
import com.youtubeagent.agent.ToolExecutionContext;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import com.youtubeagent.youtube.analytics.YouTubeVideoAnalytics;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class GetMyTopVideoAnalyticsTool implements AuthenticatedAgentTool {
    private final YouTubeAnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public GetMyTopVideoAnalyticsTool(YouTubeAnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_my_top_video_analytics";
    }

    @Override
    public String getDescription() {
        return """
                Returns a ranked analytics comparison of the connected channel's videos
                for a specified date range. Results are sorted by views, highest first,
                and include videoId, views, watch time, average view duration, likes,
                comments, subscribers gained, and subscribers lost.

                Required arguments:
                - startDate: start date in YYYY-MM-DD format
                - endDate: end date in YYYY-MM-DD format

                Optional arguments:
                - maxResults: number of videos to compare, 1-50, default 10

                Important:
                - Reports only videos belonging to the connected channel.
                - Use this tool to compare or rank multiple own-channel videos.
                - Uses one analytics report request, not one request per video.
                - Dates must use YYYY-MM-DD format and endDate is inclusive.
                - Do not invent or assume a date range. If no period is specified, ask.
                - For a relative period, call get_current_time first and calculate exact dates.
                - This is read-only. Never invent metric values.
                """;
    }

    @Override
    public Object execute(ToolExecutionContext context, Map<String, Object> arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        Object startValue = arguments.get("startDate");
        Object endValue = arguments.get("endDate");
        if (startValue == null) {
            throw new IllegalArgumentException("Missing required argument: startDate");
        }
        if (endValue == null) {
            throw new IllegalArgumentException("Missing required argument: endDate");
        }

        LocalDate start = parseDate(startValue, "startDate");
        LocalDate end = parseDate(endValue, "endDate");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        int maxResults = parseMaxResults(arguments.get("maxResults"));
        List<YouTubeVideoAnalytics> result = analyticsService.getMyTopVideoAnalytics(
                context.userId(), start, end, maxResults);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize top video analytics", e);
        }
    }

    private LocalDate parseDate(Object value, String fieldName) {
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be in YYYY-MM-DD format", e);
        }
    }

    private int parseMaxResults(Object value) {
        if (value == null) return 10;
        final int maxResults;
        try {
            maxResults = value instanceof Number n ? n.intValue() : Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("maxResults must be an integer between 1 and 50", e);
        }
        if (maxResults < 1 || maxResults > 50) {
            throw new IllegalArgumentException("maxResults must be between 1 and 50");
        }
        return maxResults;
    }
}
