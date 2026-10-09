package com.youtubeagent;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public final class RestClientTestSupport {

    private RestClientTestSupport() {
    }

    public static MockRestServiceServer install(Object target) {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(target, "restClient", builder.build());
        return server;
    }
}
