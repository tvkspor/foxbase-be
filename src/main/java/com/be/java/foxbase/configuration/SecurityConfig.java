package com.be.java.foxbase.configuration;

import com.be.java.foxbase.handler.GoogleAuthSuccessHandler;
import com.be.java.foxbase.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"
    };

    private final String[] PUBLIC_EXPLORE_ENDPOINTS = {
            "/books/{id}", "/books/genre", "books/title", "books/author"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private GoogleAuthSuccessHandler googleAuthSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
                request -> request.requestMatchers(HttpMethod.POST, PUBLIC_AUTH_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .authenticated())
                .authorizeHttpRequests(
                    request -> request.requestMatchers(HttpMethod.GET, PUBLIC_EXPLORE_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Custom OAuth2UserService if needed
                        )
                        .successHandler(googleAuthSuccessHandler) // Optional: handle successful login
                );

        httpSecurity.csrf(AbstractHttpConfigurer::disable);


        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
