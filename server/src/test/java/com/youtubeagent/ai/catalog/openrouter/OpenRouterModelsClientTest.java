package com.youtubeagent.ai.catalog.openrouter;

import com.youtubeagent.RestClientTestSupport;
import com.youtubeagent.config.OpenRouterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenRouterModelsClientTest {

    @Test
    void filtersCatalogToFreeTextModelsWithToolCalling() {
        OpenRouterModelsClient client = new OpenRouterModelsClient(
                new OpenRouterProperties("key", "http://openrouter.test"));
        var server = RestClientTestSupport.install(client);
        server.expect(requestTo(containsString("/v1/models")))
                .andRespond(withSuccess("""
                        {"data":[
                          {"id":"model-good","name":"Good","context_length":128000,
                           "pricing":{"prompt":"0","completion":"0"},
                           "supported_parameters":["tools","tool_choice"],
                           "architecture":{"input_modalities":["text"],"output_modalities":["text"]}},
                          {"id":"openrouter/free","name":"Sentinel","pricing":{"prompt":"0","completion":"0"},
                           "supported_parameters":["tools","tool_choice"],
                           "architecture":{"input_modalities":["text"],"output_modalities":["text"]}},
                          {"id":"model-paid","name":"Paid","pricing":{"prompt":"1","completion":"1"},
                           "supported_parameters":["tools","tool_choice"],
                           "architecture":{"input_modalities":["text"],"output_modalities":["text"]}}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        var models = client.getFreeToolCallingModels();

        assertEquals(1, models.size());
        assertEquals("model-good", models.getFirst().id());
        server.verify();
    }
}
