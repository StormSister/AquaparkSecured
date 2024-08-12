package com.example.aquaparksecured.config;



import com.example.aquaparksecured.user.AppUser;
import com.example.aquaparksecured.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AquaparkUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user by username: " + username);

        AppUser user = userRepository.findByEmail(username).orElseThrow(() -> {
            System.out.println("User not found with username: " + username);
            return new UsernameNotFoundException("Username " + username);
        });

        System.out.println("User found: " + user);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        System.out.println("Authorities: " + authorities);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();

        System.out.println("Returning UserDetails: " + userDetails);
        return userDetails;
    }
}