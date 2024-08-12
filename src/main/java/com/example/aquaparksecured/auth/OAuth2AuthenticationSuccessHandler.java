package com.example.aquaparksecured.auth;

import com.example.aquaparksecured.constans.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Environment env;

    public OAuth2AuthenticationSuccessHandler(Environment env) {
        this.env = env;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Ustaw Authentication do SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generowanie JWT Tokena
        if (null != authentication) {
            String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            String jwt = Jwts.builder()
                    .issuer("Aquapark")
                    .subject("JWT Token")
                    .claim("username", email)
                    .claim("authorities", authentication.getAuthorities().stream().map(
                            authority -> authority.getAuthority()).collect(Collectors.joining(",")))
                    .issuedAt(new Date())
                    .expiration(new Date((new Date()).getTime() + 30000000))
                    .signWith(secretKey)
                    .compact();

            // Przekierowanie na frontend z tokenem w URL
            String redirectUrl = "http://localhost:3000/dashboard?token=" + jwt;
            response.sendRedirect(redirectUrl);
        }
    }
}

