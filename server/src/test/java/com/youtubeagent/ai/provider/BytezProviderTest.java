package com.youtubeagent.ai.provider;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.config.BytezProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BytezProviderTest {

    @Test
    void returnsFirstChoiceContent() {
        BytezProvider provider = new BytezProvider(new BytezProperties("key", "http://bytez.test"));
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/v1/chat/completions")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"Bytez response"}}]}
                        """, MediaType.APPLICATION_JSON));

        var response = provider.generate(new LLMRequest(
                "model-1", List.of(new LLMMessage("user", "Hello")), List.of()));

        assertEquals("bytez", provider.getProviderId());
        assertEquals("Bytez response", response.content());
        server.verify();
    }
}
