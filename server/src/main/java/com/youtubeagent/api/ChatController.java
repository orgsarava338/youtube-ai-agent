package com.youtubeagent.api;

import com.youtubeagent.agent.chat.Conversation;
import com.youtubeagent.agent.chat.ConversationService;
import com.youtubeagent.auth.CurrentUserId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ConversationService conversationService;

    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ChatResponse createConversation(
            @Valid @RequestBody ChatRequest request,
            @CurrentUserId String userId) {

        return sendMessage(null, request, userId);
    }

    @PostMapping("/{conversationId}")
    public ChatResponse sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatRequest request,
            @CurrentUserId String userId) {

        return sendMessageInternal(conversationId, request, userId);
    }

    @GetMapping
    public List<ConversationSummary> getConversations(@CurrentUserId String userId) {

        return conversationService.getConversations(userId)
                .stream()
                .map(c -> new ConversationSummary(
                        c.getId(),
                        c.getTitle(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()))
                .toList();
    }

    @GetMapping("/{conversationId}")
    public ConversationDetail getConversation(
            @PathVariable UUID conversationId,
            @CurrentUserId String userId) {

        ConversationService.ConversationHistory history = conversationService.getConversation(conversationId, userId);

        Conversation conversation = history.conversation();

        return new ConversationDetail(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                history.messages().stream()
                        .map(m -> new MessageResponse(m.role(), m.content()))
                        .toList());
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationId, @CurrentUserId String userId) {

        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    private ChatResponse sendMessageInternal(UUID conversationId, ChatRequest request, String userId) {

        log.info("user: {}, Conversation Id: {}, request: {}", userId, conversationId, request.message());

        ConversationService.ChatResult result = conversationService.chat(
                userId,
                conversationId,
                request.message());

        return new ChatResponse(
                result.conversationId(),
                result.response().type(),
                result.response().content());
    }

    public record ChatRequest(
            @NotBlank(message = "Message is required") @Size(max = 20000, message = "Message must not exceed 20000 characters") String message) {
    }

    public record ChatResponse(
            UUID conversationId,
            String type,
            String content) {
    }

    public record ConversationSummary(
            UUID conversationId,
            String title,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ConversationDetail(
            UUID conversationId,
            String title,
            Instant createdAt,
            Instant updatedAt,
            List<MessageResponse> messages) {
    }

    public record MessageResponse(
            String role,
            String content) {
    }
}