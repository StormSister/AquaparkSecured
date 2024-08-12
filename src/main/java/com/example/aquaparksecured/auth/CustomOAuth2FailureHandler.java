package com.example.aquaparksecured.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        String redirectUrl = "/login?oauth_error=true";
        String lastAttemptedUrl = request.getRequestURI();

        if (lastAttemptedUrl.contains("github")) {
            redirectUrl = "/oauth2/authorization/github";
        } else if (lastAttemptedUrl.contains("facebook")) {
            redirectUrl = "/oauth2/authorization/facebook";
        } else if (lastAttemptedUrl.contains("google")) {
            redirectUrl = "/oauth2/authorization/google";
        }

        response.sendRedirect(redirectUrl);
    }
}