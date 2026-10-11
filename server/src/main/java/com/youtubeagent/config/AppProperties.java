
package com.youtubeagent.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String allowedOrigins) {

    public List<String> allowedOriginList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(origin -> origin.trim())
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
