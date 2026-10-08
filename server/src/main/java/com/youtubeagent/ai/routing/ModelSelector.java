package com.youtubeagent.ai.routing;

import com.youtubeagent.ai.model.ModelDefinition;
import com.youtubeagent.ai.model.ModelRequirements;
import com.youtubeagent.ai.model.ModelRole;

import java.util.List;

public interface ModelSelector {

    List<ModelDefinition> select(ModelRole role, ModelRequirements requirements);
}