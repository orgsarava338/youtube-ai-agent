package com.youtubeagent.agent.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findByConversation_IdOrderBySequenceNoAsc(UUID conversationId);

    @Query("""
            select coalesce(max(m.sequenceNo), 0)
            from ConversationMessage m
            where m.conversation.id = :conversationId
            """)
    long findLastSequence(@Param("conversationId") UUID conversationId);
}