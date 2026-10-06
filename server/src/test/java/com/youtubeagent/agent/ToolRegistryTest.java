package com.youtubeagent.agent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToolRegistryTest {

    @Test
    void shouldExecuteRegisteredTool() {

        AgentTool tool = mock(AgentTool.class);

        when(tool.getName()).thenReturn("test_tool");
        when(tool.getDescription()).thenReturn("Test tool");
        when(tool.execute(Map.of("value", "hello")))
                .thenReturn("result");

        ToolRegistry registry = new ToolRegistry(List.of(tool));

        String result = registry.execute(
                "test_tool",
                Map.of("value", "hello"));

        assertEquals("result", result);

        verify(tool).execute(Map.of("value", "hello"));
    }

    @Test
    void shouldRejectUnknownTool() {

        AgentTool tool = mock(AgentTool.class);

        when(tool.getName()).thenReturn("known_tool");

        ToolRegistry registry = new ToolRegistry(List.of(tool));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registry.execute(
                        "unknown_tool",
                        Map.of()));

        assertEquals(
                "Unknown tool: unknown_tool",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankToolName() {

        ToolRegistry registry = new ToolRegistry(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.execute(
                        " ",
                        Map.of()));
    }

    @Test
    void shouldUseEmptyArgumentsWhenArgumentsAreNull() {

        AgentTool tool = mock(AgentTool.class);

        when(tool.getName()).thenReturn("test_tool");
        when(tool.execute(Map.of())).thenReturn("ok");

        ToolRegistry registry = new ToolRegistry(List.of(tool));

        assertEquals(
                "ok",
                registry.execute("test_tool", null));

        verify(tool).execute(Map.of());
    }

    @Test
    void shouldReturnNoToolsMessage() {

        ToolRegistry registry = new ToolRegistry(List.of());

        assertEquals(
                "No tools available.",
                registry.getToolDescriptions());
    }
}