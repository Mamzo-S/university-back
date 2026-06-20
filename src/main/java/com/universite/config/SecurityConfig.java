package com.universite.config;

import com.universite.security.JwtFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] STAFF_AUTHORITIES = {
            "ADMIN",
            "FORMATEUR",
            "PERSONNEL_ADMIN",
            "TUTEUR",
            "RESPONSABLE_FORMATION",
            "SERVICE_INSERTION"
    };

    private static final String[] TEACHING_AUTHORITIES = {
            "ADMIN",
            "FORMATEUR",
            "RESPONSABLE_FORMATION"
    };

    private final JwtFilter jwtFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/logout",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/filieres/**")
                        .hasAnyAuthority("ADMIN", "FORMATEUR", "RESPONSABLE_FORMATION")
                        .requestMatchers("/api/filieres/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers("/api/cours/**")
                        .hasAnyAuthority(TEACHING_AUTHORITIES)
                        .requestMatchers(HttpMethod.GET, "/api/notes/bulletins/me/**", "/api/notes/me")
                        .hasAuthority("ETUDIANT")
                        .requestMatchers("/api/notes/**")
                        .hasAnyAuthority("ADMIN", "FORMATEUR", "RESPONSABLE_FORMATION", "ETUDIANT")
                        .requestMatchers(HttpMethod.GET, "/api/stages/me")
                        .hasAuthority("ETUDIANT")
                        .requestMatchers("/api/partenaires/**", "/api/stages/**")
                        .hasAnyAuthority("ADMIN", "SERVICE_INSERTION")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**")
                        .hasAnyAuthority(STAFF_AUTHORITIES)
                        .requestMatchers(HttpMethod.GET, "/api/etudiants/me/**")
                        .hasAuthority("ETUDIANT")
                        .requestMatchers(HttpMethod.GET, "/api/etudiants/**")
                        .hasAnyAuthority(STAFF_AUTHORITIES)
                        .requestMatchers("/api/etudiants/**")
                        .hasAnyAuthority(TEACHING_AUTHORITIES)
                        .requestMatchers(HttpMethod.GET, "/api/formations", "/api/formations/**")
                        .authenticated()
                        .requestMatchers("/api/formations/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(HttpMethod.GET, "/api/promotions/**")
                        .hasAnyAuthority(STAFF_AUTHORITIES)
                        .requestMatchers("/api/promotions/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(HttpMethod.GET, "/api/annees-academiques/**")
                        .hasAnyAuthority("ADMIN", "FORMATEUR", "RESPONSABLE_FORMATION", "ETUDIANT")
                        .requestMatchers("/api/annees-academiques/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(HttpMethod.GET, "/api/groupes-etudiants/**")
                        .hasAnyAuthority("ADMIN", "FORMATEUR", "RESPONSABLE_FORMATION")
                        .requestMatchers("/api/groupes-etudiants/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(HttpMethod.GET, "/api/emplois-du-temps/**")
                        .hasAnyAuthority("ADMIN", "FORMATEUR", "RESPONSABLE_FORMATION", "ETUDIANT")
                        .requestMatchers("/api/emplois-du-temps/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(HttpMethod.GET, "/api/formateurs/**")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION", "FORMATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/formateurs/*/formations")
                        .hasAnyAuthority("ADMIN", "RESPONSABLE_FORMATION")
                        .requestMatchers(
                                "/api/administrateurs/**",
                                "/api/formateurs/**",
                                "/api/tuteurs/**",
                                "/api/responsables-formation/**",
                                "/api/services-insertion/**"
                        )
                        .hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
