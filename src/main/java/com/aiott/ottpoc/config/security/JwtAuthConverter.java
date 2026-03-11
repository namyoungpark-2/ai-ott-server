package com.aiott.ottpoc.config.security;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String requiredAud;

    public JwtAuthConverter(String requiredAud) {
        this.requiredAud = Objects.requireNonNull(requiredAud);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        enforceAudience(jwt);

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(extractScopes(jwt)); // "job:retry"
        authorities.addAll(extractRoles(jwt));  // "ROLE_ADMIN"

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private void enforceAudience(Jwt jwt) {
        List<String> aud = jwt.getAudience();
        if (aud == null || aud.isEmpty() || !aud.contains(requiredAud)) {
            // aud가 안 맞으면 해당 체인(admin/ops/app) 접근 금지
            throw new IllegalArgumentException("Invalid aud. required=" + requiredAud + ", actual=" + aud);
        }
    }

    private Set<GrantedAuthority> extractScopes(Jwt jwt) {
        Object raw = jwt.getClaims().get("scopes");
        if (raw == null) return Set.of();

        List<String> scopes;
        if (raw instanceof Collection<?> c) {
            scopes = c.stream().map(String::valueOf).toList();
        } else {
            scopes = Arrays.stream(String.valueOf(raw).split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
        }

        return scopes.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    private Set<GrantedAuthority> extractRoles(Jwt jwt) {
        Object raw = jwt.getClaims().get("roles");
        if (raw == null) return Set.of();

        List<String> roles;
        if (raw instanceof Collection<?> c) {
            roles = c.stream().map(String::valueOf).toList();
        } else {
            roles = List.of(String.valueOf(raw));
        }

        return roles.stream()
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }
}
