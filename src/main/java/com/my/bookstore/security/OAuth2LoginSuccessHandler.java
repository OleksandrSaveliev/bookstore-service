package com.my.bookstore.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.my.bookstore.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.oauth2-callback-path}")
    private String oauthCallbackPath;

    @Value("${app.frontend.login-path}")
    private String loginPath;

    public OAuth2LoginSuccessHandler(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OAuth2User principal)) {
            log.error("OAuth2 login succeeded but principal is {}", authentication.getPrincipal().getClass().getName());
            response.sendRedirect(buildFrontendUrl(loginPath, "error", "oauth_invalid_principal"));
            return;
        }

        try {
            String email = principal.getAttribute("email");
            log.info("OAuth2 login successful for email: {}", email);

            authService.processOAuthPostLogin(principal, response);

            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, buildFrontendUrl(oauthCallbackPath));

        } catch (Exception e) {
            log.error("Error processing OAuth2 post-login", e);
            response.sendRedirect(buildFrontendUrl(loginPath, "error", "oauth_processing_failed"));
        }
    }

    private String buildFrontendUrl(String path) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(path)
                .build()
                .toUriString();
    }

    private String buildFrontendUrl(String path, String queryParam, String value) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(path)
                .queryParam(queryParam, value)
                .build()
                .toUriString();
    }
}
