package com.example.aquaparksecured.config;

import com.example.aquaparksecured.auth.CustomOAuth2FailureHandler;
import com.example.aquaparksecured.auth.CustomOAuth2SuccessHandler;
import com.example.aquaparksecured.auth.CustomOAuth2UserService;
import com.example.aquaparksecured.exceptionhandling.CustomAccessDeniedHandler;
import com.example.aquaparksecured.filter.JWTTokenGeneratorFilter;
import com.example.aquaparksecured.filter.JWTTokenValidatorFilter;
import com.example.aquaparksecured.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@Profile("!prod")
public class ProjectSecurityConfiguration {


    @Autowired
    private Environment env;

    @Autowired
    private CustomOAuth2FailureHandler customOAuth2FailureHandler;


    @Autowired
    private UserRepository userRepository;


    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();

        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfig -> corsConfig.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Arrays.asList("Authorization"));
                    config.setMaxAge(3600L);
                    return config;
                }))
                .csrf().disable()
//                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
//                        .ignoringRequestMatchers("/contact", "/register", "/user", "/apiLogin", "/apiLoginFacebook", "/secure","/login/oauth2/code/github", "/apiLoginFacebook")
//
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
//                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
//                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // HTTP
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/oauth2/**","/prices","/tickets/ticket-types", "/confirm-payment", "/login/oauth2/**",  "/error", "/register", "/invalidSession", "/apiLogin", "/favicon.ico",
                                "/static/**", "/oauth2/**", "/login/oauth2/code/**", "/create-checkout-session","/email/send-email",
                                "/login/oauth2/code/github","/promotions/current","/uploads/**", "/rooms/available", "/reservations", "/login", "/tickets/purchase").permitAll()
                        .requestMatchers("/user", "/secure", "/reservations/api/all", "reservations/api/user", "prices/api/add", "/prices/api/delete/{priceId}","/prices/api/update/{id}", "/tickets/api/check-qr", "promotions/api/add").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        );
        http.formLogin().disable();
//         http.oauth2Login(Customizer.withDefaults());
        http.oauth2Login(oauth2Login ->
                oauth2Login
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.userService(customOAuth2UserService())
                        )
                        .defaultSuccessUrl("/login-success", true)
                        .failureHandler(customOAuth2FailureHandler)

                        .successHandler(customOAuth2SuccessHandler())
        );
//        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
        http.logout(logout -> logout
                .logoutUrl("/logout"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(userRepository, passwordEncoder());
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }

//    @Bean
//    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
//        return new OAuth2AuthenticationSuccessHandler(env);
//    }

    @Configuration
    public class JacksonConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper;
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        AquaparkUsernamePwdAuthenticationProvider authenticationProvider =
                new AquaparkUsernamePwdAuthenticationProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

    @Bean
    public CustomOAuth2SuccessHandler customOAuth2SuccessHandler() {
        return new CustomOAuth2SuccessHandler(env, userRepository, passwordEncoder());
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login?error=true");
    }

//      @Bean
//    ClientRegistrationRepository clientRegistrationRepository() {
//        ClientRegistration github = githubClientRegistration();
//        ClientRegistration facebook = facebookClientRegistration();
//        return new InMemoryClientRegistrationRepository(github, facebook);
//    }
//
//    private ClientRegistration githubClientRegistration() {
//        return CommonOAuth2Provider.GITHUB.getBuilder("github").clientId("Ov23cttR7FRTprWVpX9f")
//                .clientSecret("cfcffd4943a9f4a9098a3a72556a6f6ad294fab2").build();
//    }
//
//    private ClientRegistration facebookClientRegistration() {
//        return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook").clientId("467033489628413")
//                .clientSecret("8cb25d97082c0a57190c77738bbebc3c").build();
//    }
}