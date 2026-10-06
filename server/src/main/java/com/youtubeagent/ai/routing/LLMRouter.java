package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelRequirements;

public interface LLMRouter {

    LLMResponse generate(LLMRequest request, ModelRequirements requirements);
}