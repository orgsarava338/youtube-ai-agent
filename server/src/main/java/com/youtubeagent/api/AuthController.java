
package com.youtubeagent.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/login")
    public RedirectView login() {
        RedirectView redirectView = new RedirectView("/oauth2/authorization/google");
        redirectView.setContextRelative(true);
        return redirectView;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal OidcUser user) {
        return new UserResponse(user.getSubject(), user.getName(), user.getEmail(), user.getPicture());
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName());
    }

    private record UserResponse(
            String id,
            String name,
            String email,
            String picture) {
    }

    private record CsrfResponse(
            String token,
            String headerName) {
    }
}
