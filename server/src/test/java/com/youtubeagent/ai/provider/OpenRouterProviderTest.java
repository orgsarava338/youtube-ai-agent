package com.youtubeagent.ai.provider;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.config.OpenRouterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenRouterProviderTest {

    @Test
    void convertsReturnedToolCallsToTheCommonResponseFormat() {
        OpenRouterProvider provider = new OpenRouterProvider(
                new OpenRouterProperties("key", "http://provider.test"), new ObjectMapper());
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/v1/chat/completions")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":null,
                        "tool_calls":[{"id":"call-1","type":"function","function":{
                        "name":"search_videos","arguments":"{\\\"query\\\":\\\"cats\\\"}"}}]}}]}
                        """, MediaType.APPLICATION_JSON));

        var response = provider.generate(request());

        assertEquals("openrouter", provider.getProviderId());
        assertTrue(response.hasToolCalls());
        assertEquals("call-1", response.toolCalls().getFirst().id());
        assertEquals("search_videos", response.toolCalls().getFirst().name());
        assertEquals("cats", response.toolCalls().getFirst().arguments().get("query"));
        server.verify();
    }

    @Test
    void rejectsAnEmptyProviderResponse() {
        OpenRouterProvider provider = new OpenRouterProvider(
                new OpenRouterProperties("key", "http://provider.test"), new ObjectMapper());
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/v1/chat/completions")))
                .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> provider.generate(request()));
        server.verify();
    }

    private static LLMRequest request() {
        return new LLMRequest("model-1", List.of(new LLMMessage("user", "Find cats")), List.of());
    }
}
