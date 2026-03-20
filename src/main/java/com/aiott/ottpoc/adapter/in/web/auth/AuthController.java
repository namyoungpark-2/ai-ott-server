package com.aiott.ottpoc.adapter.in.web.auth;

import com.aiott.ottpoc.config.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider tokens;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtTokenProvider tokens,
                          JdbcTemplate jdbc,
                          PasswordEncoder passwordEncoder) {
        this.tokens = tokens;
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequest req) {
        if (req.username() == null || req.username().isBlank()
                || req.password() == null || req.password().length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "아이디와 비밀번호(4자 이상)를 입력해주세요."));
        }

        Integer exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE username = ?",
                Integer.class, req.username());
        if (exists != null && exists > 0) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "이미 사용 중인 아이디입니다."));
        }

        String hash = passwordEncoder.encode(req.password());
        var row = jdbc.queryForMap(
                "INSERT INTO app_user (username, password_hash) VALUES (?, ?) "
                        + "RETURNING id, username, role, subscription_tier",
                req.username(), hash);

        String jwt = tokens.issueAppToken(
                String.valueOf(row.get("id")),
                (String) row.get("role"));

        return ResponseEntity.ok(Map.of(
                "accessToken", jwt,
                "id", String.valueOf(row.get("id")),
                "username", row.get("username"),
                "role", row.get("role"),
                "subscriptionTier", row.get("subscription_tier")
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> appLogin(@RequestBody LoginRequest req) {
        var rows = jdbc.queryForList(
                "SELECT id, username, password_hash, role, subscription_tier "
                        + "FROM app_user WHERE username = ?",
                req.username());

        if (rows.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }

        var user = rows.get(0);
        if (!passwordEncoder.matches(req.password(), (String) user.get("password_hash"))) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }

        String jwt = tokens.issueAppToken(
                String.valueOf(user.get("id")),
                (String) user.get("role"));

        return ResponseEntity.ok(Map.of(
                "accessToken", jwt,
                "id", String.valueOf(user.get("id")),
                "username", user.get("username"),
                "role", user.get("role"),
                "subscriptionTier", user.get("subscription_tier")
        ));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<TokenResponse> adminLogin(@RequestBody LoginRequest req) {
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
