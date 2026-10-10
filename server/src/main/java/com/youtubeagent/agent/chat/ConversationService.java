package com.youtubeagent.agent.chat;

import com.youtubeagent.agent.AgentResponse;
import com.youtubeagent.agent.AgentService;
import com.youtubeagent.agent.AgentTurnResult;
import com.youtubeagent.ai.core.LLMMessage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationStore conversationStore;
    private final AgentService agentService;

    public ConversationService(ConversationStore conversationStore, AgentService agentService) {

        this.conversationStore = conversationStore;
        this.agentService = agentService;
    }

    public ChatResult chat(String userId, UUID conversationId, String userMessage) {

        if (conversationId == null) {
            Conversation conversation = conversationStore.create(userId, userMessage);
            conversationId = conversation.getId();
        } else {
            conversationStore.getOwnedConversation(conversationId, userId);
        }

        List<LLMMessage> history = new ArrayList<>(conversationStore.getMessages(conversationId, userId));

        LLMMessage userMessageEntry = new LLMMessage("user", userMessage);

        conversationStore.append(conversationId, userId, List.of(userMessageEntry));
        history.add(userMessageEntry);

        AgentTurnResult turnResult = agentService.runTurn(userId, userMessage, history);

        conversationStore.append(conversationId, userId, turnResult.messages());

        return new ChatResult(conversationId, turnResult.response());
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversations(String userId) {
        return conversationStore.getConversations(userId);
    }

    @Transactional(readOnly = true)
    public ConversationHistory getConversation(UUID conversationId, String userId) {
        Conversation conversation = conversationStore.getOwnedConversation(conversationId, userId);
        List<LLMMessage> messages = conversationStore.getMessages(conversationId, userId);

        return new ConversationHistory(conversation, messages);
    }

    public record ConversationHistory(
            Conversation conversation,
            List<LLMMessage> messages) {
    }

    public record ChatResult(
            UUID conversationId,
            AgentResponse response) {
    }
}