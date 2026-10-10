package com.youtubeagent.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youtubeagent.agent.AgentResponse;
import com.youtubeagent.agent.AgentService;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request,  @AuthenticationPrincipal OidcUser user) {
        if (user == null || user.getSubject() == null || user.getSubject().isBlank()) {
            throw new IllegalStateException("Authenticated Google user not found");
        }

        AgentResponse agentResponse = agentService.chat(user.getSubject(), request.message());
        return new ChatResponse(agentResponse.type(), agentResponse.content());
    }

    private record ChatRequest(
        @NotBlank(message = "Message is required")
        @Size(min = 1, max = 20000, message = "Message must be between 1 and 20000 characters")
        String message) {
    }

    public record ChatResponse(
        String type,
        String content) {
    }
}
