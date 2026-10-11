package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.ToolCall;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RepeatedToolCallGuardTest {
    private final RepeatedToolCallGuard guard = new RepeatedToolCallGuard(new ObjectMapper());

    @Test
    void detectsSameToolAndArgumentsWithinOneTurn() {
        var tracker = guard.newTurnTracker();
        assertFalse(tracker.isRepeated(new ToolCall("1", "calculate", Map.of("expression", "2 + 2"))));
        assertTrue(tracker.isRepeated(new ToolCall("2", "calculate", Map.of("expression", "2 + 2"))));
    }

    @Test
    void canonicalizesNestedMapKeyOrder() {
        var tracker = guard.newTurnTracker();
        var first = new ToolCall("1", "tool", Map.of("outer", Map.of("b", 2, "a", 1),
                "list", List.of(Map.of("z", true, "x", "value"))));
        var second = new ToolCall("2", "tool", Map.of("list", List.of(Map.of("x", "value", "z", true)),
                "outer", Map.of("a", 1, "b", 2)));
        assertFalse(tracker.isRepeated(first));
        assertTrue(tracker.isRepeated(second));
    }

    @Test
    void allowsDifferentArgumentsAndResetsForANewTurn() {
        var firstTurn = guard.newTurnTracker();
        assertFalse(firstTurn.isRepeated(new ToolCall("1", "calculate", Map.of("expression", "1 + 1"))));
        assertFalse(firstTurn.isRepeated(new ToolCall("2", "calculate", Map.of("expression", "1 + 2"))));
        assertFalse(firstTurn.isRepeated(new ToolCall("3", "get_my_channel", Map.of())));
        assertFalse(guard.newTurnTracker().isRepeated(
                new ToolCall("4", "calculate", Map.of("expression", "1 + 1"))));
    }
}
