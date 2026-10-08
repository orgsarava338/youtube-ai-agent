package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMRequest;

public interface ResponseModel {

    String generate(LLMRequest request);
}