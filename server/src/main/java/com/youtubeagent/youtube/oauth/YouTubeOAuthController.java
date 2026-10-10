package com.youtubeagent.youtube.oauth;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youtube/oauth")
public class YouTubeOAuthController {

    private static final String OAUTH_STATE_SESSION_KEY = "youtube_oauth_state";

    private final YouTubeOAuthService oauthService;

    public YouTubeOAuthController(YouTubeOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/start")
    public ResponseEntity<Void> start(HttpSession session) {

        String state = oauthService.createState();

        session.setAttribute(OAUTH_STATE_SESSION_KEY, state);

        String authorizationUrl = oauthService.buildAuthorizationUrl(state);

        return ResponseEntity
                .status(302)
                .header("Location", authorizationUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state, HttpSession session,
            @AuthenticationPrincipal OidcUser user) {

        Object storedState = session.getAttribute(OAUTH_STATE_SESSION_KEY);

        if (storedState == null || !storedState.equals(state)) {
            return ResponseEntity.badRequest().body("Invalid OAuth state");
        }

        if (user == null || user.getSubject() == null || user.getSubject().isBlank()) {
            return ResponseEntity.status(401).body("Sign in before connecting YouTube.");
        }

        session.removeAttribute(OAUTH_STATE_SESSION_KEY);

        GoogleTokenResponse tokenResponse = oauthService.exchangeCode(code);
        oauthService.saveToken(user.getSubject(), tokenResponse);

        return ResponseEntity.ok("YouTube account connected successfully.");
    }
}