package com.example.aquaparksecured.auth;

import com.example.aquaparksecured.user.AppUser;
import com.example.aquaparksecured.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());

        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null) {
            throw new IllegalArgumentException("Email attribute is not found in the OAuth2User");
        }

        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {

            System.out.println("Creating new user with email: " + email);
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setUsername(oAuth2User.getAttribute("login"));
            newUser.setPassword(passwordEncoder.encode("defaultPassword"));
            newUser.setRole("client");
            userRepository.save(newUser);
        } else {

            System.out.println("User found with email: " + email);
        }

        AppUser appUser = userRepository.findByEmail(email).orElseThrow();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        return new DefaultOAuth2User(Collections.singletonList(new OAuth2UserAuthority(attributes)),
                attributes, "email");
    }
}
