package com.aiott.ottpoc.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;

    public SecurityConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    @Order(0)
    SecurityFilterChain authChain(HttpSecurity http) throws Exception {
      http.securityMatcher("/auth/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain adminPublicChain(HttpSecurity http) throws Exception {
        http.securityMatcher(
                "/api/admin/health",
                "/api/admin/video-assets/**",
                "/api/admin/failures",
                "/api/admin/failures/**"
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/admin/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(new JwtAuthConverter("admin"))
                )
            );
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain opsChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/ops/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/ops/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(new JwtAuthConverter("ops"))
                )
            );
        return http.build();
    }

    @Bean
    @Order(4)
    SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/app/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/app/analytics/**").authenticated()
                .anyRequest().permitAll()   // ✅ 핵심: app은 기본 공개
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(new JwtAuthConverter("app"))
                )
            );
        return http.build();
    }


    // /auth/** 같은 게 생기면 여기에 permitAll로 열어두면 됨
    @Bean
    @Order(99)
    SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
              .requestMatchers(
                  "/auth/**",
                  "/health",
                  "/actuator/health",
                  "/api/admin/**",
                  "/hls/**",
                  "/thumbnails/**"
              ).permitAll()
                .anyRequest().denyAll()
            );
        return http.build();
    }
}
