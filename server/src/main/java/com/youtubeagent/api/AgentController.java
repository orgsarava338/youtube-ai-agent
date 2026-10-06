package com.youtubeagent.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final com.youtubeagent.agent.AgentService agentService;

    public AgentController(com.youtubeagent.agent.AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        com.youtubeagent.agent.AgentResponse agentResponse = agentService.chat(request.message());
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
