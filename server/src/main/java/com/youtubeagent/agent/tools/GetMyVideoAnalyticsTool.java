package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.analytics.YouTubeAnalytics;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class GetMyVideoAnalyticsTool implements AgentTool {

    private final YouTubeAnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public GetMyVideoAnalyticsTool(YouTubeAnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_my_video_analytics";
    }

    @Override
    public String getDescription() {
        return """
                Returns analytics for a specific video on the connected
                YouTube channel over a specified date range.

                Required arguments:
                - videoId: the actual YouTube video ID
                - startDate: start date in YYYY-MM-DD format
                - endDate: end date in YYYY-MM-DD format

                The end date is inclusive.

                Returned metrics include:
                - views
                - estimatedMinutesWatched
                - averageViewDurationSeconds
                - likes
                - comments
                - subscribersGained
                - subscribersLost

                Important:
                - Use only an actual videoId returned by a YouTube tool.
                - Never invent or guess a videoId.
                - Dates must use YYYY-MM-DD format.
                - startDate must not be after endDate.
                - Do not invent or assume a date range.
                - If the user has not specified an analytics period,
                  ask which period they want.
                - For a relative period, call get_current_time first
                  and calculate the exact dates from its result.
                - This is a read-only operation.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null) {
            throw new IllegalArgumentException("videoId, startDate and endDate are required");
        }

        Object videoIdValue = arguments.get("videoId");
        Object startDateValue = arguments.get("startDate");
        Object endDateValue = arguments.get("endDate");

        if (videoIdValue == null || videoIdValue.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required argument: videoId");
        }

        if (startDateValue == null) {
            throw new IllegalArgumentException("Missing required argument: startDate");
        }

        if (endDateValue == null) {
            throw new IllegalArgumentException("Missing required argument: endDate");
        }

        LocalDate startDate = parseDate(startDateValue, "startDate");
        LocalDate endDate = parseDate(endDateValue, "endDate");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        YouTubeAnalytics analytics = analyticsService.getMyVideoAnalytics(
                videoIdValue.toString().trim(),
                startDate,
                endDate);

        try {
            return objectMapper.writeValueAsString(analytics);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize video analytics", e);
        }
    }

    private LocalDate parseDate(Object value, String fieldName) {
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be in YYYY-MM-DD format", e);
        }
    }
}