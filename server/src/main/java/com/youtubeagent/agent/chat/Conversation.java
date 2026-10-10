package com.youtubeagent.agent.chat;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(schema = "chat", name = "conversations")
public class Conversation {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(length = 200)
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Conversation() {
    }

    public static Conversation create(String userId, String title) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Conversation conversation = new Conversation();
        conversation.id = UUID.randomUUID();
        conversation.userId = userId;
        conversation.title = normalizeTitle(title);

        return conversation;
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "New conversation";
        }

        String normalized = title.strip();

        return normalized.length() <= 200
                ? normalized
                : normalized.substring(0, 200);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}