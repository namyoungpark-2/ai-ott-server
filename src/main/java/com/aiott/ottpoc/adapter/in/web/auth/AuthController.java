package com.aiott.ottpoc.adapter.in.web.auth;

import com.aiott.ottpoc.config.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider tokens;

    public AuthController(JwtTokenProvider tokens) {
        this.tokens = tokens;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<TokenResponse> adminLogin(@RequestBody LoginRequest req) {
        // POC: 하드코딩 검증 (운영은 user table + password hash)
        if (!"admin".equals(req.username()) || !"admin".equals(req.password())) {
            return ResponseEntity.status(401).build();
        }
        String jwt = tokens.issueAdminToken("admin_1");
        return ResponseEntity.ok(new TokenResponse(jwt));
    }

    @PostMapping("/ops/login")
    public ResponseEntity<TokenResponse> opsLogin(@RequestBody LoginRequest req) {
        if (!"ops".equals(req.username()) || !"ops".equals(req.password())) {
            return ResponseEntity.status(401).build();
        }
        String jwt = tokens.issueOpsToken("ops_1");
        return ResponseEntity.ok(new TokenResponse(jwt));
    }

    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String accessToken) {}
}
