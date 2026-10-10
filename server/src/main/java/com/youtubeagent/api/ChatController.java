package com.youtubeagent.api;

import com.youtubeagent.agent.chat.Conversation;
import com.youtubeagent.agent.chat.ConversationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
            @AuthenticationPrincipal OidcUser user) {

        return sendMessage(null, request, user);
    }

    @PostMapping("/{conversationId}")
    public ChatResponse sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal OidcUser user) {

        return sendMessageInternal(conversationId, request, user);
    }

    @GetMapping
    public List<ConversationSummary> getConversations(
            @AuthenticationPrincipal OidcUser user) {

        String userId = requireUserId(user);

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
            @AuthenticationPrincipal OidcUser user) {

        String userId = requireUserId(user);

        ConversationService.ConversationHistory history = conversationService.getConversation(conversationId, userId);

        Conversation conversation = history.conversation();

        return new ConversationDetail(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                history.messages().stream()
                        .map(m -> new MessageResponse(
                                m.role(),
                                m.content(),
                                m.toolCallId()))
                        .toList());
    }

    private ChatResponse sendMessageInternal(UUID conversationId, ChatRequest request, OidcUser user) {

        String userId = requireUserId(user);

        ConversationService.ChatResult result = conversationService.chat(
                userId,
                conversationId,
                request.message());

        return new ChatResponse(
                result.conversationId(),
                result.response().type(),
                result.response().content());
    }

    private String requireUserId(OidcUser user) {
        if (user == null || user.getSubject() == null || user.getSubject().isBlank()) {
            throw new IllegalStateException("Authenticated Google user not found");
        }

        return user.getSubject();
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
            String content,
            String toolCallId) {
    }
}