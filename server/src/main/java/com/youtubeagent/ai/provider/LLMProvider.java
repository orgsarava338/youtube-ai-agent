package com.youtubeagent.ai.provider;

import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.ai.core.LLMResponse;

public interface LLMProvider {

    String getProviderId();

    LLMResponse generate(LLMRequest request);
}