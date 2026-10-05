package com.youtubeagent.api;

import com.youtubeagent.agent.AgentResponse;
import com.youtubeagent.agent.AgentService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public AgentResponse chat(@RequestBody String message) {
        return agentService.chat(message);
    }
}