package com.aiott.ottpoc.adapter.in.web.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            // 인증 없이 접근 시 기본 admin 정보 반환 (개발용)
            return new MeResponse("admin_1", null, "admin", "ROLE_ADMIN");
        }
        String sub = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        return new MeResponse(
                sub,
                null,
                sub,
                roles != null && !roles.isEmpty() ? roles.get(0) : null
        );
    }

    public record MeResponse(
            String id,
            String email,
            String name,
            String role
    ) {}
}
