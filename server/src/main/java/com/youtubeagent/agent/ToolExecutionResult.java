package com.youtubeagent.agent;

public record ToolExecutionResult(
        String callId,
        String tool,
        String result,
        boolean success) {

    public static ToolExecutionResult success(String callId, String tool, String result) {
        return new ToolExecutionResult(callId, tool, result, true);
    }

    public static ToolExecutionResult failure(String callId, String tool, String result) {
        return new ToolExecutionResult(callId, tool, result, false);
    }
}