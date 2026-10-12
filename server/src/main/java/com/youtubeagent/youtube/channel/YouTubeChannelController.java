package com.youtubeagent.youtube.channel;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youtubeagent.auth.CurrentUserId;

@RestController
@RequestMapping("/api/v1/youtube")
public class YouTubeChannelController {

    private final YouTubeChannelService channelService;

    public YouTubeChannelController(YouTubeChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/channel")
    public YouTubeChannel getMyChannel(@CurrentUserId String userId) {
        return channelService.getMyChannel(userId);
    }

    @GetMapping("/channels")
    public List<YouTubeChannel> getAllMyChannels(@CurrentUserId String userId) {
        return channelService.getAllMyChannels(userId);
    }
    
}