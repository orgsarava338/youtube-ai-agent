package com.youtubeagent.agent.chat;

import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMToolCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationStore {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public ConversationStore(
            ConversationRepository conversationRepository,
            ConversationMessageRepository messageRepository,
            ObjectMapper objectMapper) {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Conversation create(String userId, String title) {
        Conversation conversation = Conversation.create(userId, title);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Conversation getOwnedConversation(UUID conversationId, String userId) {

        return conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    }

    @Transactional(readOnly = true)
    public List<LLMMessage> getMessages(UUID conversationId, String userId) {

        getOwnedConversation(conversationId, userId);

        List<ConversationMessage> storedMessages = messageRepository
                .findByConversation_IdOrderBySequenceNoAsc(conversationId);

        List<LLMMessage> messages = new ArrayList<>(storedMessages.size());

        for (ConversationMessage stored : storedMessages) {
            List<LLMToolCall> toolCalls = readToolCalls(stored.getToolCallsJson());

            messages.add(new LLMMessage(
                    stored.getRole(),
                    stored.getContent(),
                    toolCalls,
                    stored.getToolCallId()));
        }

        return List.copyOf(messages);
    }

    @Transactional
    public void append(UUID conversationId, String userId, List<LLMMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            return;
        }

        Conversation conversation = getOwnedConversation(conversationId, userId);

        long sequence = messageRepository.findLastSequence(conversationId);

        List<ConversationMessage> messageEntities = new ArrayList<>(messages.size());

        for (LLMMessage message : messages) {
            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null");
            }

            String toolCallsJson = writeToolCalls(message);

            messageEntities.add(ConversationMessage.create(
                    conversation,
                    ++sequence,
                    message.role(),
                    message.content(),
                    toolCallsJson,
                    message.toolCallId()));
        }

        messageRepository.saveAll(messageEntities);

        // Ensure the parent conversation is updated too.
        conversation.touch();
        conversationRepository.save(conversation);
    }

    private String writeToolCalls(LLMMessage message) {
        if (!message.hasToolCalls()) {
            return null;
        }

        return objectMapper.writeValueAsString(message.toolCalls());
    }

    private List<LLMToolCall> readToolCalls(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        LLMToolCall[] toolCalls = objectMapper.readValue(json, LLMToolCall[].class);

        return List.copyOf(Arrays.asList(toolCalls));
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }
}