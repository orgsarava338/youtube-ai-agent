package com.youtubeagent.youtube.oauth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YouTubeOAuthDebugController {

    private final YouTubeOAuthService oauthService;

    public YouTubeOAuthDebugController(
            YouTubeOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/api/v1/youtube/oauth/token")
    public YouTubeToken getScope() {
        return oauthService.getStoredToken();
    }
}