package com.youtubeagent.agent;

import com.youtubeagent.ai.core.LLMToolDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolRegistryTest {

    @Mock private AgentTool publicTool;
    @Mock private AuthenticatedAgentTool authenticatedTool;
    @Mock private ObjectMapper objectMapper;

    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        when(publicTool.getName()).thenReturn("echo");
        when(authenticatedTool.getName()).thenReturn("my_channel");
        registry = new ToolRegistry(List.of(publicTool), List.of(authenticatedTool), objectMapper);
    }

    @Test
    void executesPublicToolAndPassesArguments() {
        Map<String, Object> arguments = Map.of("text", "hello");
        when(publicTool.execute(arguments)).thenReturn("hello");

        assertEquals("hello", registry.execute(null, "echo", arguments));
        verify(publicTool).execute(arguments);
    }

    @Test
    void serializesNonStringPublicToolResults() throws Exception {
        when(publicTool.execute(any())).thenReturn(Map.of("count", 3));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"count\":3}");

        assertEquals("{\"count\":3}", registry.execute(null, "echo", Map.of()));
    }

    @Test
    void returnsEmptyStringForNullToolResult() {
        when(publicTool.execute(any())).thenReturn(null);

        assertEquals("", registry.execute(null, "echo", null));
        verify(publicTool).execute(Map.of());
    }

    @Test
    void executesAuthenticatedToolWithUserContext() {
        ToolExecutionContext context = new ToolExecutionContext("user-123");
        Map<String, Object> arguments = Map.of("includeUploads", true);
        when(authenticatedTool.execute(context, arguments)).thenReturn("channel");

        assertEquals("channel", registry.execute(context, "my_channel", arguments));
        verify(authenticatedTool).execute(context, arguments);
    }

    @Test
    void rejectsAuthenticatedToolWithoutExecutionContext() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registry.execute(null, "my_channel", Map.of()));

        assertEquals("Execution context is required for authenticated tools.", exception.getMessage());
        verify(authenticatedTool, never()).execute(any(), any());
    }

    @Test
    void rejectsAuthenticatedToolWhenUserIsNotAuthenticated() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registry.execute(new ToolExecutionContext(" "), "my_channel", Map.of()));

        assertEquals("Authentication is required to execute this tool.", exception.getMessage());
        verify(authenticatedTool, never()).execute(any(), any());
    }

    @Test
    void rejectsUnknownOrBlankToolNames() {
        assertThrows(IllegalArgumentException.class, () -> registry.execute(null, "missing", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> registry.execute(null, " ", Map.of()));
        assertFalse(registry.hasTool(null));
        assertFalse(registry.hasTool(" "));
        assertFalse(registry.hasTool("missing"));
    }

    @Test
    void exposesToolDefinitionsAndDescriptions() {
        when(publicTool.getDescription()).thenReturn("Returns a value.");
        when(authenticatedTool.getDescription()).thenReturn("Reads the user's channel.");

        assertTrue(registry.hasTool("echo"));
        assertTrue(registry.hasTool("my_channel"));
        assertEquals(2, registry.getLLMToolDefinitions().size());

        LLMToolDefinition definition = registry.getLLMToolDefinitions().getFirst();
        assertEquals("echo", definition.name());
        assertEquals("Returns a value.", definition.description());
        assertEquals("object", definition.parameters().get("type"));
        assertTrue(registry.getToolDescriptions().contains("echo | Returns a value."));
        assertEquals(2, registry.getTools().size());
    }

    @Test
    void emptyRegistryHasHelpfulDescription() {
        ToolRegistry emptyRegistry = new ToolRegistry(List.of(), List.of(), objectMapper);

        assertEquals("No tools available.", emptyRegistry.getToolDescriptions());
        assertTrue(emptyRegistry.getTools().isEmpty());
        assertTrue(emptyRegistry.getLLMToolDefinitions().isEmpty());
    }
}
