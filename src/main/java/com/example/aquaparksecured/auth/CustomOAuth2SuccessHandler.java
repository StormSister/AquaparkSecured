package com.example.aquaparksecured.auth;

import com.example.aquaparksecured.constans.ApplicationConstants;
import com.example.aquaparksecured.user.AppUser;
import com.example.aquaparksecured.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final Environment env;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;


            OAuth2User oauthUser = oauthToken.getPrincipal();
            String email = (String) oauthUser.getAttributes().get("email");

            if (email != null) {
                Optional<AppUser> userOptional = userRepository.findByEmail(email);
                if (userOptional.isEmpty()) {
                    AppUser newUser = new AppUser();
                    newUser.setEmail(email);
                    newUser.setUsername(oauthUser.getAttribute("login"));
                    newUser.setPassword(passwordEncoder.encode("defaultPassword")); // Default password
                    newUser.setRole("client");
                    userRepository.save(newUser);
                }


                AppUser appUser = userRepository.findByEmail(email).orElseThrow();


                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                String jwt = Jwts.builder()
                        .setIssuer("Aquapark")
                        .setSubject(appUser.getEmail())
                        .claim("username", appUser.getEmail())
                        .claim("authorities", appUser.getRole())
                        .setIssuedAt(new java.util.Date())
                        .setExpiration(new java.util.Date((new java.util.Date()).getTime() + 3600000)) // 1 godzina
                        .signWith(secretKey)
                        .compact();
                System.out.println("jwt: " + jwt);

                String redirectUrl = "http://localhost:3000/login-success?jwtToken=" + jwt;
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            }
        }
    }
}

