package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;

public interface LLMRouter {

    LLMResponse generate(LLMRequest request, ModelRole role, ModelRequirements requirements);
}