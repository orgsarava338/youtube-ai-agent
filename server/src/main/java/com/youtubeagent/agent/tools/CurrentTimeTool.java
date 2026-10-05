package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
public class CurrentTimeTool implements AgentTool {

    @Override
    public String getName() {
        return "get_current_time";
    }

    @Override
    public String getDescription() {
        return "Returns the current date and time.";
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        return OffsetDateTime.now().toString();
    }
}