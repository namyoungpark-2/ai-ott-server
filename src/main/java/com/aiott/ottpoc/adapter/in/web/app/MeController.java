package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/me")
public class MeController {

    private final UserWatchProgressUseCase progressUseCase;

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
