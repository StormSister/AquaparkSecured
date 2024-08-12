package com.example.aquaparksecured.auth;


import com.example.aquaparksecured.constans.ApplicationConstants;
import com.example.aquaparksecured.user.AppUser;
import com.example.aquaparksecured.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor

public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment env;



    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AppUser user) {
        try {
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                user.setUsername(user.getEmail());
            }

            System.out.println("User before saving: " + user);

            String hashPwd = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashPwd);
            AppUser savedCustomer = userRepository.save(user);

            System.out.println("User after saving: " + savedCustomer);

            if (savedCustomer.getId() > 0) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Given user details are successfully registered");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User registration failed");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An exception occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequest) {
        System.out.println("Otrzymano żądanie logowania: " + loginRequest);

        String jwt = "";
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(),
                loginRequest.password());
        System.out.println("Nieautoryzowane uwierzytelnienie: " + authentication);

        Authentication authenticationResponse = authenticationManager.authenticate(authentication);
        System.out.println("Odpowiedź uwierzytelniania: " + authenticationResponse);

        if (null != authenticationResponse && authenticationResponse.isAuthenticated()) {
            System.out.println("Uwierzytelnienie powiodło się dla użytkownika: " + authenticationResponse.getName());

            if (null != env) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                System.out.println("Uzyskano tajny klucz JWT: " + secret);

                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                jwt = Jwts.builder().issuer("Aquapark").subject("JWT Token")
                        .claim("username", authenticationResponse.getName())
                        .claim("authorities", authenticationResponse.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .issuedAt(new java.util.Date())
                        .expiration(new java.util.Date((new java.util.Date()).getTime() + 30000000))
                        .signWith(secretKey).compact();
                System.out.println("Wygenerowano JWT: " + jwt);
            }
        } else {
            System.out.println("Uwierzytelnienie nie powiodło się dla użytkownika: " + loginRequest.username());
        }

        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER, jwt)
                .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));
    }



    private String generateJwtToken(AppUser appUser) {
        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .issuer("Aquapark")
                .subject("JWT Token")
                .claim("username", appUser.getEmail())
                .claim("authorities", appUser.getRole())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 30000000))
                .signWith(secretKey)
                .compact();
    }

    @RequestMapping("/user")
    public AppUser getUserDetailsAfterLogin(Authentication authentication) {
        Optional<AppUser> optionalCustomer = userRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }

    @GetMapping("/currentUser")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return ResponseEntity.ok("Zalogowany użytkownik: " + username);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Nikt nie jest zalogowany.");
    }

}

