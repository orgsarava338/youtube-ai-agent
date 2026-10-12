
package com.youtubeagent.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthTokenService authTokenService;
    private final GoogleProfileService googleProfileService;

    AuthController(AuthTokenService authTokenService, GoogleProfileService googleProfileService) {
        this.authTokenService = authTokenService;
        this.googleProfileService = googleProfileService;
    }

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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestBody(required = false) LogoutRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String accessToken = null;

        if (authorization != null
                && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7).trim();
        }

        authTokenService.logout(
                request == null ? null : request.refreshToken(),
                accessToken);

    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        GoogleProfileService.GoogleProfile user = googleProfileService.getProfile(userId);

        return ResponseEntity.ok(new UserResponse(user.id(), user.name(), user.email(), user.picture()));
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName());
    }

    @PostMapping("/token/exchange")
    public ExchangeResponse exchange(@RequestBody ExchangeRequest request) {
        var token = authTokenService.exchangeLoginCode(request.code());
        return new ExchangeResponse(
                token.accessToken(),
                token.refreshToken(),
                "Bearer",
                token.expiresIn());
    }

    @PostMapping("/token/refresh")
    public ExchangeResponse refresh(@RequestBody RefreshRequest request) {
        var tokens = authTokenService.refreshTokens(
                request == null ? null : request.refreshToken());

        return new ExchangeResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.expiresIn());
    }

    @ExceptionHandler(AuthTokenService.InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse invalidToken() {
        return new ErrorResponse("invalid_token", "The token is invalid, expired, or already used");
    }

    public record ExchangeRequest(String code) {
    }

    public record ExchangeResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn) {
    }

    public record LogoutRequest(String refreshToken) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record ErrorResponse(String error, String message) {
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
