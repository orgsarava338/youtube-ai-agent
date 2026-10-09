package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
public record DbProperties(
        String host,
        int port,
        String password,
        String database,
        String user) {
}
