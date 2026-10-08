package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMRequest;

public interface DecisionModel {

    AgentDecision decide(LLMRequest request);
}