package com.youtubeagent.youtube;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public final class YouTubeJsonUtils {

    private YouTubeJsonUtils() {
    }

    public static String textValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return null;
        }

        return node.get(field).stringValue();
    }

    public static long longValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return 0;
        }

        return node.get(field).asLong();
    }

    public static boolean booleanValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return false;
        }

        return node.get(field).asBoolean();
    }

    public static Instant instantValue(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return null;
        }

        return Instant.parse(textValue(node, field));
    }

    public static String thumbnailUrl(JsonNode snippet) {
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

}