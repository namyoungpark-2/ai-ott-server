package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminUserSummary;
import com.aiott.ottpoc.application.port.in.AdminUserCommandUseCase;
import com.aiott.ottpoc.application.port.in.AdminUserQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin-only user management endpoints.
 * All routes under /api/admin/** are secured to ROLE_ADMIN by SecurityConfig.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserQueryUseCase queryUseCase;
    private final AdminUserCommandUseCase commandUseCase;

    /**
     * GET /api/admin/users?tier=BASIC
     * Returns all users, optionally filtered by subscription tier.
     */
    @GetMapping
    public List<AdminUserSummary> listUsers(
            @RequestParam(name = "tier", required = false) String tier) {
        return queryUseCase.listUsers(tier);
    }

    /**
     * PUT /api/admin/users/{id}/subscription
     * Body: { "tier": "PREMIUM" }
     * Force-changes a user's subscription tier.
     */
    @PutMapping("/{id}/subscription")
    public ResponseEntity<Map<String, String>> changeSubscription(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String newTier = body.get("tier");
        if (newTier == null || newTier.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "tier is required"));
        }
        try {
            commandUseCase.changeSubscriptionTier(id, newTier);
            return ResponseEntity.ok(Map.of("message", "Subscription updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/users/{id}
     * Hard-deletes the user account and associated watch data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID id) {
        commandUseCase.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
