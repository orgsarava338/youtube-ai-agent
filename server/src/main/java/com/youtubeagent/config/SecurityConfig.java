
package com.youtubeagent.config;

import com.youtubeagent.api.RequestLoggingFilter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.youtubeagent.youtube.token.YouTubeOAuthSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {
    private final RequestLoggingFilter requestLoggingFilter;
    private final AppProperties appProperties;

    SecurityConfig(AppProperties appProperties, RequestLoggingFilter requestLoggingFilter) {
        this.appProperties = appProperties;
        this.requestLoggingFilter = requestLoggingFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            OAuth2AuthorizationRequestResolver authorizationRequestResolver,
            YouTubeOAuthSuccessHandler youTubeOAuthSuccessHandler) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/health",
                                "/error",
                                "/actuator/health",
                                "/oauth2/**",
                                "/login/**",
                                "/api/v1/auth/login")
                        .permitAll()
                        .anyRequest().authenticated())

                .oauth2Login(oauth -> oauth.authorizationEndpoint(endPoint -> endPoint
                        .authorizationRequestResolver(authorizationRequestResolver))
                        .successHandler(youTubeOAuthSuccessHandler)
                // .defaultSuccessUrl(appProperties.frontendUrl(), true)
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response
                                .setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))

                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                (RequestMatcher) request -> request.getRequestURI().startsWith("/api")));

        // Keep Spring Security's CSRF protection enabled.
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(appProperties.frontendUrl()));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(
                List.of("Content-Type", "X-CSRF-TOKEN", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(
                builder -> builder.additionalParameters(parameters -> {
                    parameters.put("access_type", "offline");
                    parameters.put("prompt", "consent select_account");
                }));

        return resolver;
    }

}
