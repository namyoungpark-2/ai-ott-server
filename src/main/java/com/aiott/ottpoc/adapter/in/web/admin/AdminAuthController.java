package com.aiott.ottpoc.adapter.in.web.admin;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<String> scopes = jwt.getClaimAsStringList("scopes");

        return new MeResponse(
                sub,
                null, // email – POC에서는 JWT에 없음
                sub,  // name – subject를 이름으로 사용
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
