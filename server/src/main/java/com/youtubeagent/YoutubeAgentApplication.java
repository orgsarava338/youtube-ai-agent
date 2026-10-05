package com.youtubeagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class YoutubeAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeAgentApplication.class, args);
    }
}