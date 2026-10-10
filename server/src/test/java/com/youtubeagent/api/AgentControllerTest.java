package com.youtubeagent.api;

import com.youtubeagent.agent.AgentResponse;
import com.youtubeagent.agent.AgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AgentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
        })
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @Test
    void shouldReturnAgentResponse() throws Exception {

        when(agentService.chat("hello"))
                .thenReturn(
                        new AgentResponse(
                                "final_answer",
                                null,
                                "Hello!"));

        mockMvc.perform(
                post("/api/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "hello"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type")
                        .value("final_answer"))
                .andExpect(jsonPath("$.content")
                        .value("Hello!"));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {

        mockMvc.perform(
                post("/api/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingMessage() throws Exception {

        mockMvc.perform(
                post("/api/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnServiceError() throws Exception {

        when(agentService.chat(anyString()))
                .thenThrow(
                        new IllegalStateException(
                                "All suitable LLM models failed"));

        mockMvc.perform(
                post("/api/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "hello"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type")
                        .value("agent_error"));
    }
}