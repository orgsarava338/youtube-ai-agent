package com.youtubeagent.youtube.oauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeTokenTest {

    @Test
    void shouldBeExpiredWhenExpiryIsWithin60Seconds() {

        long expiresAt = (System.currentTimeMillis() / 1000) + 30;

        YouTubeToken token = new YouTubeToken(
                "access",
                "refresh",
                expiresAt,
                "scope",
                "Bearer");

        assertTrue(token.isExpired());
    }

    @Test
    void shouldNotBeExpiredWhenExpiryIsMoreThan60SecondsAway() {

        long expiresAt = (System.currentTimeMillis() / 1000) + 300;

        YouTubeToken token = new YouTubeToken(
                "access",
                "refresh",
                expiresAt,
                "scope",
                "Bearer");

        assertFalse(token.isExpired());
    }

    @Test
    void shouldBeExpiredWhenAlreadyExpired() {

        long expiresAt = (System.currentTimeMillis() / 1000) - 100;

        YouTubeToken token = new YouTubeToken(
                "access",
                "refresh",
                expiresAt,
                "scope",
                "Bearer");

        assertTrue(token.isExpired());
    }
}