package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe 웹훅 수신 엔드포인트.
 *
 * POST /api/webhooks/stripe
 *
 * - 인증 불필요 (Stripe이 직접 호출)
 * - 서명은 PaymentService 내부에서 검증
 * - 원본 바디(byte[])를 그대로 전달해야 서명 검증이 가능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    private final PaymentService paymentService;

    @PostMapping(value = "/stripe", consumes = "application/json")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("ok");
        } catch (SecurityException e) {
            log.warn("[WEBHOOK] Rejected: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            log.error("[WEBHOOK] Error processing event: {}", e.getMessage(), e);
            // Stripe는 200이 아니면 재시도하므로, 처리 오류는 200 반환
            return ResponseEntity.ok("accepted");
        }
    }
}
