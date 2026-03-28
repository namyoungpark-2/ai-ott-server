package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.WatchProgressItem;
import com.aiott.ottpoc.application.port.in.UserWatchProgressUseCase;
import com.aiott.ottpoc.application.port.out.UserAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/me")
public class MeController {

    private final UserWatchProgressUseCase progressUseCase;
    private final UserAuthPort userAuthPort;
    private final PasswordEncoder passwordEncoder;

    // ── 내 프로필 ──────────────────────────────────────────

    @GetMapping
    public MyProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userAuthPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return new MyProfileResponse(user.id(), user.username(), user.role(),
                user.subscriptionTier(), user.emailVerified());
    }

    public record MyProfileResponse(UUID id, String username, String role,
                                     String subscriptionTier, boolean emailVerified) {}

    @PutMapping
    public Map<String, String> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String newUsername = body.get("username");
        if (newUsername != null && !newUsername.isBlank()) {
            if (userAuthPort.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already taken: " + newUsername);
            }
            userAuthPort.updateUsername(userId, newUsername);
        }
        return Map.of("message", "updated");
    }

    @PutMapping("/password")
    public Map<String, String> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("currentPassword and newPassword are required");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        var user = userAuthPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.passwordHash())) {
            throw new SecurityException("Current password is incorrect");
        }

        userAuthPort.updatePassword(userId, passwordEncoder.encode(newPassword));
        return Map.of("message", "password changed");
    }

    @DeleteMapping
    public Map<String, String> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userAuthPort.deleteUser(userId);
        return Map.of("message", "account deleted");
    }

    // ── 시청 기록 ──────────────────────────────────────────

    @GetMapping("/continue-watching")
    public List<WatchProgressItem> continueWatching(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String lang) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return progressUseCase.getContinueWatching(userId, LangResolver.resolve(lang));
    }

    @GetMapping("/playback-progress/{contentId}")
    public Map<String, Long> getProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID contentId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        long posMs = progressUseCase.getPosition(userId, contentId);
        return Map.of("positionMs", posMs);
    }

    @PostMapping("/playback-progress/{contentId}")
    public ResponseEntity<Map<String, String>> saveProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID contentId,
            @RequestBody ProgressRequest body) {
        UUID userId = UUID.fromString(jwt.getSubject());
        progressUseCase.saveProgress(userId, contentId, body.positionMs(), body.durationMs());
        return ResponseEntity.ok(Map.of("message", "Progress saved"));
    }

    public record ProgressRequest(long positionMs, Long durationMs) {}
}
