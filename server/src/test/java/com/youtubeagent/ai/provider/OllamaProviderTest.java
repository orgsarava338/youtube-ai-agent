package com.youtubeagent.ai.provider;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.ai.core.LLMMessage;
import com.youtubeagent.ai.core.LLMRequest;
import com.youtubeagent.config.OllamaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaProviderTest {

    @Test
    void returnsGeneratedText() {
        OllamaProvider provider = new OllamaProvider(new OllamaProperties("http://ollama.test", "model"));
        var server = RestClientTestSupport.install(provider);
        server.expect(requestTo(containsString("/api/generate")))
                .andRespond(withSuccess("{\"response\":\"Local response\"}", MediaType.APPLICATION_JSON));

        var response = provider.generate(new LLMRequest(
                "llama", List.of(new LLMMessage("user", "Hello")), List.of()));

        assertEquals("ollama", provider.getProviderId());
        assertEquals("Local response", response.content());
        assertEquals("llama", response.model());
        server.verify();
    }
}
