package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;

import java.util.List;

public interface ModelSelector {

    List<ModelDefinition> select(ModelRequirements requirements);
}