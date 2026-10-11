
package com.youtubeagent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class OriginOAuthAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String SESSION_ATTRIBUTE = "ASSISTUBE_ALLOWED_ORIGIN";

    private final OAuth2AuthorizationRequestResolver delegate;
    private final AppProperties appProperties;

    public OriginOAuthAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
            AppProperties appProperties) {

        var defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization");

        defaultResolver.setAuthorizationRequestCustomizer(
                builder -> builder.additionalParameters(parameters -> {
                    parameters.put("access_type", "offline");
                    parameters.put("prompt", "consent select_account");
                }));

        this.delegate = defaultResolver;
        this.appProperties = appProperties;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return captureOrigin(request, delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return captureOrigin(request, delegate.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest captureOrigin(HttpServletRequest request,
            OAuth2AuthorizationRequest authorizationRequest) {

        if (authorizationRequest == null) {
            return null;
        }

        String redirectUrl = request.getParameter("redirectUrl");

        if (redirectUrl == null || redirectUrl.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_request"),
                    "Missing redirect URL");
        }

        String origin = redirectUrl.trim();

        if (!appProperties.allowedOriginList().contains(origin)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_request"),
                    "Redirect URL is not allowed");
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_ATTRIBUTE, origin);

        return authorizationRequest;
    }
}
