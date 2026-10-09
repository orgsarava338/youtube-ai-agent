package com.youtubeagent.youtube.token;

import com.youtubeagent.youtube.oauth.YouTubeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class YouTubeTokenPersistenceService {

    private final YouTubeTokenRepository repository;
    private final YouTubeTokenCrypto crypto;

    public YouTubeTokenPersistenceService(YouTubeTokenRepository repository, YouTubeTokenCrypto crypto) {
        this.repository = repository;
        this.crypto = crypto;
    }

    @Transactional
    public void save(String userId, YouTubeToken token) {
        YouTubeTokenEntity entity = repository.findByUserId(userId)
                .orElseGet(() -> {
                    YouTubeTokenEntity newEntity = new YouTubeTokenEntity();
                    newEntity.setUserId(userId);
                    return newEntity;
                });

        entity.setAccessTokenEncrypted(crypto.encrypt(token.accessToken()));

        // Preserve an existing refresh token if the provider omits it.
        if (token.refreshToken() != null) {
            entity.setRefreshTokenEncrypted(crypto.encrypt(token.refreshToken()));
        }

        entity.setExpiresAt(Instant.ofEpochSecond(token.expiresAtEpochSeconds()));
        entity.setScopes(token.scope());
        entity.setTokenType(token.tokenType());

        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<YouTubeToken> load(String userId) {
        return repository.findByUserId(userId)
                .map(entity -> new YouTubeToken(
                        crypto.decrypt(entity.getAccessTokenEncrypted()),
                        crypto.decrypt(entity.getRefreshTokenEncrypted()),
                        entity.getExpiresAt() == null
                                ? 0L
                                : entity.getExpiresAt().getEpochSecond(),
                        entity.getScopes(),
                        entity.getTokenType()));
    }

    @Transactional
    public void delete(String userId) {
        repository.deleteByUserId(userId);
    }
}