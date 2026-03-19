package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.WatchProgressItem;
import com.aiott.ottpoc.application.port.in.UserWatchProgressUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User self-service endpoints (all require authentication).
 *
 * GET  /api/app/me/continue-watching             – in-progress content list
 * GET  /api/app/me/playback-progress/{contentId} – position for a single content
 * POST /api/app/me/playback-progress/{contentId} – save / update position
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/me")
public class MeController {

    private final UserWatchProgressUseCase progressUseCase;

    @GetMapping("/continue-watching")
    public List<WatchProgressItem> continueWatching(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "lang", defaultValue = "en") String lang) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return progressUseCase.getContinueWatching(userId, lang);
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
