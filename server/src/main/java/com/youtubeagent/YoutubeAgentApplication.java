package com.youtubeagent;

import com.youtubeagent.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class YoutubeAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeAgentApplication.class, args);
    }
}