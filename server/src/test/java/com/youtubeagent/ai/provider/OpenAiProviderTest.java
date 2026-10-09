package com.youtubeagent.ai.provider;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiProviderTest {

    @Test
    void sendsChatRequestAndReturnsAssistantContent() {
        OpenAiProvider provider = new OpenAiProvider(new OpenAiProperties("key", "http://provider.test", "model"));
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/v1/chat/completions")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"Hello from OpenAI"}}]}
                        """, MediaType.APPLICATION_JSON));

        var response = provider.generate(request());

        assertEquals("openai", provider.getProviderId());
        assertEquals("Hello from OpenAI", response.content());
        assertEquals("model-1", response.model());
        server.verify();
    }

    private static LLMRequest request() {
        return new LLMRequest("model-1", List.of(new LLMMessage("user", "Hello")), List.of());
    }
}
