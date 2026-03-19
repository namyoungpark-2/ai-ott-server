package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 결제 관련 API 엔드포인트.
 *
 * POST /api/app/payments/checkout  - Stripe Checkout 세션 생성
 * POST /api/app/payments/webhook   - Stripe 웹훅 수신 (인증 불필요, 서명 검증)
 * POST /api/app/payments/portal    - Stripe Customer Portal (구독 관리)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // ─── Checkout Session ──────────────────────────────────────────────────

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CheckoutRequest req) {

        if (req.plan() == null || (!req.plan().equalsIgnoreCase("BASIC") && !req.plan().equalsIgnoreCase("PREMIUM"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "플랜은 BASIC 또는 PREMIUM 이어야 합니다."));
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        String userEmail = jwt.getClaimAsString("sub"); // username이 이메일이라고 가정

        try {
            String url = paymentService.createCheckoutSession(userId, userEmail, req.plan());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Stripe Customer Portal ────────────────────────────────────────────

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> openPortal(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PortalRequest req) {

        if (req.stripeCustomerId() == null || req.stripeCustomerId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "stripeCustomerId가 필요합니다."));
        }

        try {
            String url = paymentService.createPortalSession(req.stripeCustomerId());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    public record CheckoutRequest(String plan) {}
    public record PortalRequest(String stripeCustomerId) {}
}
