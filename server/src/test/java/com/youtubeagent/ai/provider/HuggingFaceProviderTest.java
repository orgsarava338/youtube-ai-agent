package com.youtubeagent.ai.provider;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.config.HuggingFaceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HuggingFaceProviderTest {

    @Test
    void returnsAssistantContent() {
        HuggingFaceProvider provider = new HuggingFaceProvider(
                new HuggingFaceProperties("http://huggingface.test", "key", "model"));
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/v1/chat/completions")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"HF response"}}]}
                        """, MediaType.APPLICATION_JSON));

        var response = provider.generate(new LLMRequest(
                "model-1", List.of(new LLMMessage("user", "Hello")), List.of()));

        assertEquals("huggingface", provider.getProviderId());
        assertEquals("HF response", response.content());
        server.verify();
    }
}
