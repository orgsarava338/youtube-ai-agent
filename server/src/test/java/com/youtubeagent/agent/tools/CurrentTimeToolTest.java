package com.youtubeagent.agent.tools;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrentTimeToolTest {

    @Test
    void shouldReturnCurrentDateTime() {

        CurrentTimeTool tool = new CurrentTimeTool();

        Object result = tool.execute(Map.of());

        assertNotNull(result);

        assertDoesNotThrow(
                () -> OffsetDateTime.parse(result.toString()));
    }

    @Test
    void shouldExposeCorrectToolName() {

        CurrentTimeTool tool = new CurrentTimeTool();

        assertEquals(
                "get_current_time",
                tool.getName());
    }
}