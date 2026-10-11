
package com.youtubeagent.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    @GetMapping("/login")
    public void login(
            @RequestParam String redirectUrl,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/oauth2/authorization/google?redirectUrl="
                        + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
    }


    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal OidcUser user) {
        return new UserResponse(user.getSubject(), user.getFullName(), user.getEmail(), user.getPicture());
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
