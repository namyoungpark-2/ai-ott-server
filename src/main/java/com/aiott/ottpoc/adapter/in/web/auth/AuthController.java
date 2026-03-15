package com.aiott.ottpoc.adapter.in.web.auth;

import com.aiott.ottpoc.application.port.out.UserAuthPort;
import com.aiott.ottpoc.config.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider tokens;
    private final UserAuthPort users;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public AuthController(JwtTokenProvider tokens, UserAuthPort users) {
        this.tokens = tokens;
        this.users = users;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody LoginRequest req) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (users.existsByUsername(req.username())) {
            return ResponseEntity.status(409).build();
        }
        String hash = bcrypt.encode(req.password());
        UserAuthPort.UserRecord saved = users.save(req.username(), hash, "USER");
        String jwt = tokens.issueUserToken(saved.id().toString(), saved.role());
        return ResponseEntity.ok(new AuthResponse(jwt, saved.id().toString(), saved.username(), saved.role()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return users.findByUsername(req.username())
                .filter(u -> bcrypt.matches(req.password(), u.passwordHash()))
                .map(u -> {
                    String jwt = tokens.issueUserToken(u.id().toString(), u.role());
                    return ResponseEntity.ok(new AuthResponse(jwt, u.id().toString(), u.username(), u.role()));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody LoginRequest req) {
        if (!"admin".equals(req.username()) || !"admin".equals(req.password())) {
            return ResponseEntity.status(401).build();
        }
        String jwt = tokens.issueAdminToken("admin_1");
        return ResponseEntity.ok(new AuthResponse(jwt, "admin_1", "admin", "ADMIN"));
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
    public record AuthResponse(String accessToken, String id, String username, String role) {}
}
