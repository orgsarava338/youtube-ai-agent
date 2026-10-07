package com.youtubeagent.youtube.oauth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.youtubeagent.youtube.YouTubeProperties;

import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedYouTubeTokenStoreTest {

    private static final String ENCRYPTION_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadToken() {

        String filePath = tempDir.resolve("youtube-token.enc").toString();

        YouTubeToken expected = new YouTubeToken(
                "access-token",
                "refresh-token",
                9999999999L,
                "https://www.googleapis.com/auth/youtube.readonly",
                "Bearer");

        YouTubeProperties properties = new YouTubeProperties(
                null,
                new YouTubeProperties.TokenStorage(filePath, ENCRYPTION_KEY));

        EncryptedYouTubeTokenStore store = new EncryptedYouTubeTokenStore(
                new ObjectMapper(),
                properties);

        store.save(expected);

        YouTubeToken actual = store.load().orElseThrow();

        assertEquals(expected.accessToken(), actual.accessToken());
        assertEquals(expected.refreshToken(), actual.refreshToken());
        assertEquals(expected.expiresAtEpochSeconds(), actual.expiresAtEpochSeconds());
        assertEquals(expected.scope(), actual.scope());
        assertEquals(expected.tokenType(), actual.tokenType());

    }

    @Test
    void shouldReturnEmptyWhenTokenFileDoesNotExist() {

        String filePath = tempDir.resolve("missing-token.enc").toString();

        YouTubeProperties properties = new YouTubeProperties(
                null,
                new YouTubeProperties.TokenStorage(filePath, ENCRYPTION_KEY));

        EncryptedYouTubeTokenStore store = new EncryptedYouTubeTokenStore(
                new ObjectMapper(),
                properties);

        assertTrue(store.load().isEmpty());
    }
}