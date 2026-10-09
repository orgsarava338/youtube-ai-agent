package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.analytics.YouTubeAnalytics;
import com.youtubeagent.youtube.analytics.YouTubeAnalyticsService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class GetMyChannelAnalyticsTool implements AgentTool {

    private final YouTubeAnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public GetMyChannelAnalyticsTool(YouTubeAnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_my_channel_analytics";
    }

    @Override
    public String getDescription() {
        return """
                Returns analytics for the connected YouTube channel
                for a specified date range.

                Required arguments:
                - startDate: start date in YYYY-MM-DD format
                - endDate: end date in YYYY-MM-DD format

                The end date is inclusive.

                Returned metrics include:
                - views: total video views
                - estimatedMinutesWatched: estimated watch time in minutes
                - averageViewDurationSeconds: average view duration in seconds
                - likes: total likes
                - comments: total comments
                - subscribersGained: subscribers gained
                - subscribersLost: subscribers lost

                Important:
                - This tool returns analytics for the connected channel only.
                - Dates must use YYYY-MM-DD format.
                - startDate must not be after endDate.
                - This is a read-only analytics operation.
                - Do not invent, guess, or assume a date range.
                - Only call this tool when the user has explicitly provided
                  a date range or a clearly defined relative period such as
                  "last 30 days", "this month", "last month", or "last year".
                - If the user has not specified an analytics period,
                  ask the user which period they want.
                - Do not use arbitrary historical dates as defaults.
                - Do not use the current date as the analytics period unless
                  the user explicitly asks for a current-day period.
                - If a relative period requires the current date, call
                  get_current_time first and use its actual result to
                  calculate the date range.
                - Never use placeholders or symbolic references for dates.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }

        Object startDateValue = arguments.get("startDate");
        Object endDateValue = arguments.get("endDate");

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

        YouTubeAnalytics analytics = analyticsService.getMyChannelAnalytics(startDate, endDate);

        try {
            return objectMapper.writeValueAsString(analytics);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube Analytics", e);
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