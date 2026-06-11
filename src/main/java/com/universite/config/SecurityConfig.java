package com.universite.config;

import com.universite.security.JwtFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // CORS avec la source configurée
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Désactive CSRF
                .csrf(csrf -> csrf.disable())

                // API REST sans session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Gestion des autorisations
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers("/api/filieres/**")
                        .hasAnyAuthority(
                                "ADMIN"
                        )

                        .requestMatchers("/api/cours/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "PROFESSEUR"
                        )

                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "PROFESSEUR",
                                "ETUDIANT"
                        )

                        .requestMatchers(HttpMethod.GET, "/api/etudiants/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "PROFESSEUR",
                                "ETUDIANT"
                        )

                        .requestMatchers("/api/etudiants/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "PROFESSEUR"
                        )

                        .requestMatchers("/api/formations/**")
                        .hasAnyAuthority(
                                "ADMIN",
                                "PROFESSEUR"
                        )

                        .anyRequest().authenticated()
                )

                // Filtre JWT
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }
}