package com.youtubeagent.youtube.channel;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    public YouTubeChannel getMyChannel(@AuthenticationPrincipal OidcUser user) {
        return channelService.getMyChannel(user.getSubject());
    }

    @GetMapping("/channels")
    public List<YouTubeChannel> getAllMyChannels(@AuthenticationPrincipal OidcUser user) {
        return channelService.getAllMyChannels(user.getSubject());
    }
    
}