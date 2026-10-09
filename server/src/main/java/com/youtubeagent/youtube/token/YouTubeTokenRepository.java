package com.youtubeagent.youtube.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YouTubeTokenRepository extends JpaRepository<YouTubeTokenEntity, Long> {

    Optional<YouTubeTokenEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);

    void deleteByUserId(String userId);
}