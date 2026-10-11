package com.youtubeagent.agent.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import com.youtubeagent.agent.ToolCall;

import tools.jackson.databind.ObjectMapper;

@Component
public class RepeatedToolCallGuard {

    private final ObjectMapper objectMapper;

    public RepeatedToolCallGuard(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TurnTracker newTurnTracker() {
        return new TurnTracker();
    }

    public final class TurnTracker {

        private final Set<String> executedSignatures = new HashSet<>();

        private TurnTracker() {
        }

        /**
         * Returns true when this tool-and-arguments combination
         * has already been encountered during the current turn.
         */
        public boolean isRepeated(ToolCall toolCall) {
            String signature = buildSignature(toolCall);
            return !executedSignatures.add(signature);
        }

        private String buildSignature(ToolCall toolCall) {
            try {
                Object canonicalArguments = canonicalize(toolCall.arguments());

                return toolCall.tool() + ":" + objectMapper.writeValueAsString(canonicalArguments);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to build tool call signature for: " + toolCall.tool(), e);
            }
        }

        private Object canonicalize(Object value) {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> sorted = new TreeMap<>();

                map.forEach((key, nestedValue) -> sorted.put(String.valueOf(key), canonicalize(nestedValue)));
                return sorted;
            }

            if (value instanceof List<?> list) {
                return list.stream().map(this::canonicalize).toList();
            }

            return value;
        }
    }
}
