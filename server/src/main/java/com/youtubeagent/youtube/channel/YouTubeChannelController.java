package com.youtubeagent.youtube.channel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youtube")
public class YouTubeChannelController {

    private final YouTubeChannelService channelService;

    public YouTubeChannelController(YouTubeChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/channel")
    public YouTubeChannel getMyChannel() {
        return channelService.getMyChannel();
    }
}