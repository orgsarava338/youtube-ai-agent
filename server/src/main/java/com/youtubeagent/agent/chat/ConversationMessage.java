package com.youtubeagent.agent.chat;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.Getter;

@Getter
@Entity
@Table(schema = "chat", name = "conversation_messages", uniqueConstraints = {
        @UniqueConstraint(name = "uq_conversation_message_sequence", columnNames = { "conversation_id", "sequence_no" })
})
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "sequence_no", nullable = false)
    private long sequenceNo;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "tool_calls_json", columnDefinition = "TEXT")
    private String toolCallsJson;

    @Column(name = "tool_call_id", length = 255)
    private String toolCallId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ConversationMessage() {
    }

    public static ConversationMessage create(
            Conversation conversation,
            long sequenceNo,
            String role,
            String content,
            String toolCallsJson,
            String toolCallId) {

        if (conversation == null) {
            throw new IllegalArgumentException("Conversation is required");
        }

        if (sequenceNo < 1) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }

        if (role == null || !java.util.Set.of("system", "user", "assistant", "tool").contains(role)) {
            throw new IllegalArgumentException("Invalid message role");
        }

        ConversationMessage message = new ConversationMessage();
        message.conversation = conversation;
        message.sequenceNo = sequenceNo;
        message.role = role;
        message.content = content;
        message.toolCallsJson = toolCallsJson;
        message.toolCallId = toolCallId;

        return message;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
    }
}