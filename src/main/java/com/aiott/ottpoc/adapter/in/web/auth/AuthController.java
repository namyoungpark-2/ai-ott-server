package com.aiott.ottpoc.adapter.in.web.auth;

import com.aiott.ottpoc.application.port.out.UserAuthPort;
import com.aiott.ottpoc.application.service.EmailService;
import com.aiott.ottpoc.config.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtTokenProvider tokens;
    private final UserAuthPort users;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    @Value("${OPS_USERNAME:ops}")
    private String opsUsername;

    @Value("${OPS_PASSWORD:ops}")
    private String opsPassword;

    public AuthController(JwtTokenProvider tokens, UserAuthPort users, EmailService emailService) {
        this.tokens = tokens;
        this.users = users;
        this.emailService = emailService;
    }

    // ─── 회원가입 ──────────────────────────────────────────────────────────────

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @RequestBody SignupRequest req,
            HttpServletResponse response) {

        if (isBlank(req.username()) || isBlank(req.password())) {
            return ResponseEntity.badRequest().build();
        }
        if (users.existsByUsername(req.username())) {
            return ResponseEntity.status(409).build();
        }

        String hash = bcrypt.encode(req.password());
        UserAuthPort.UserRecord saved = users.save(req.username(), hash, "USER");

        // 이메일 인증 토큰 발급 (메일 활성화 시)
        if (mailEnabled) {
            String token = generateToken();
            users.saveEmailVerificationToken(saved.id(), token, OffsetDateTime.now().plusHours(24));
            emailService.sendVerificationEmail(saved.username(), token);
        }

        String jwt = tokens.issueUserToken(saved.id().toString(), saved.role(), saved.subscriptionTier());
        setAuthCookie(response, jwt);
        return ResponseEntity.ok(new AuthResponse(jwt, saved.id().toString(), saved.username(), saved.role(), saved.subscriptionTier()));
    }

    // ─── 로그인 ───────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest req,
            HttpServletResponse response) {

        return users.findByUsername(req.username())
                .filter(u -> bcrypt.matches(req.password(), u.passwordHash()))
                .map(u -> {
                    String jwt = tokens.issueUserToken(u.id().toString(), u.role(), u.subscriptionTier());
                    setAuthCookie(response, jwt);
                    return ResponseEntity.ok(new AuthResponse(jwt, u.id().toString(), u.username(), u.role(), u.subscriptionTier()));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    // ─── 로그아웃 ──────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ResponseEntity.ok().build();
    }

    // ─── 이메일 인증 ───────────────────────────────────────────────────────────

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestBody TokenRequest req) {
        if (isBlank(req.token())) {
            return ResponseEntity.badRequest().body(new MessageResponse("인증 토큰이 필요합니다."));
        }

        var userOpt = users.findByEmailVerificationToken(req.token());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(400).body(new MessageResponse("유효하지 않은 인증 토큰입니다."));
        }

        var user = userOpt.get();
        if (user.emailVerificationExpiresAt() != null
                && user.emailVerificationExpiresAt().isBefore(OffsetDateTime.now())) {
            return ResponseEntity.status(400).body(new MessageResponse("인증 토큰이 만료되었습니다. 재발송을 요청하세요."));
        }

        users.markEmailVerified(user.id());
        log.info("[AUTH] Email verified for user={}", user.username());
        return ResponseEntity.ok(new MessageResponse("이메일 인증이 완료되었습니다."));
    }

    // ─── 이메일 인증 재발송 ────────────────────────────────────────────────────

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestBody UsernameRequest req) {
        users.findByUsername(req.username()).ifPresent(user -> {
            if (!user.emailVerified()) {
                String token = generateToken();
                users.saveEmailVerificationToken(user.id(), token, OffsetDateTime.now().plusHours(24));
                emailService.sendVerificationEmail(user.username(), token);
            }
        });
        // 보안상 항상 동일한 응답 반환 (사용자 존재 여부 노출 방지)
        return ResponseEntity.ok(new MessageResponse("이메일이 존재하면 인증 메일을 발송했습니다."));
    }

    // ─── 비밀번호 재설정 요청 ──────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody UsernameRequest req) {
        users.findByUsername(req.username()).ifPresent(user -> {
            String token = generateToken();
            users.savePasswordResetToken(user.id(), token, OffsetDateTime.now().plusHours(1));
            emailService.sendPasswordResetEmail(user.username(), token);
            log.info("[AUTH] Password reset token issued for user={}", user.username());
        });
        // 보안상 항상 동일한 응답 반환 (사용자 존재 여부 노출 방지)
        return ResponseEntity.ok(new MessageResponse("이메일이 존재하면 비밀번호 재설정 메일을 발송했습니다."));
    }

    // ─── 비밀번호 재설정 확인 ─────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest req) {
        if (isBlank(req.token()) || isBlank(req.newPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("토큰과 새 비밀번호가 필요합니다."));
        }
        if (req.newPassword().length() < 8) {
            return ResponseEntity.badRequest().body(new MessageResponse("비밀번호는 최소 8자 이상이어야 합니다."));
        }

        var userOpt = users.findByPasswordResetToken(req.token());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(400).body(new MessageResponse("유효하지 않은 재설정 토큰입니다."));
        }

        var user = userOpt.get();
        if (user.passwordResetExpiresAt() != null
                && user.passwordResetExpiresAt().isBefore(OffsetDateTime.now())) {
            return ResponseEntity.status(400).body(new MessageResponse("재설정 토큰이 만료되었습니다. 다시 요청하세요."));
        }

        users.updatePassword(user.id(), bcrypt.encode(req.newPassword()));
        users.clearPasswordResetToken(user.id());
        log.info("[AUTH] Password reset successful for user={}", user.username());
        return ResponseEntity.ok(new MessageResponse("비밀번호가 성공적으로 변경되었습니다."));
    }

    // ─── 관리자 로그인 ────────────────────────────────────────────────────────

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody LoginRequest req) {
        if (!adminUsername.equals(req.username()) || !adminPassword.equals(req.password())) {
            return ResponseEntity.status(401).build();
        }
        String jwt = tokens.issueAdminToken("admin_1");
        return ResponseEntity.ok(new AuthResponse(jwt, "admin_1", adminUsername, "ADMIN", "PREMIUM"));
    }

    // ─── Ops 로그인 ───────────────────────────────────────────────────────────

    @PostMapping("/ops/login")
    public ResponseEntity<TokenResponse> opsLogin(@RequestBody LoginRequest req) {
        if (!opsUsername.equals(req.username()) || !opsPassword.equals(req.password())) {
            return ResponseEntity.status(401).build();
        }
        String jwt = tokens.issueOpsToken("ops_1");
        return ResponseEntity.ok(new TokenResponse(jwt));
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────────────────

    /**
     * httpOnly 쿠키 설정. 브라우저가 자동으로 포함해 XSS로 토큰 탈취 방지.
     * SameSite=Lax는 CSRF를 방지하면서 일반 링크 클릭 이동은 허용.
     */
    private void setAuthCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("auth_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);          // HTTPS 전용 (운영환경)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24시간
        response.addCookie(cookie);
        // SameSite는 Cookie API가 지원하지 않아 헤더로 직접 추가
        response.addHeader("Set-Cookie",
                "auth_token=" + jwt + "; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=86400");
    }

    private void clearAuthCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "auth_token=; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=0");
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // ─── 요청/응답 DTO ────────────────────────────────────────────────────────

    public record LoginRequest(String username, String password) {}
    public record SignupRequest(String username, String password) {}
    public record TokenRequest(String token) {}
    public record UsernameRequest(String username) {}
    public record ResetPasswordRequest(String token, String newPassword) {}

    public record TokenResponse(String accessToken) {}
    public record MessageResponse(String message) {}
    public record AuthResponse(
            String accessToken,
            String id,
            String username,
            String role,
            String subscriptionTier
    ) {}
}
