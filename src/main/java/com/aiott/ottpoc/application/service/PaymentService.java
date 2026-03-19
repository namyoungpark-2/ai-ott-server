package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.out.UserSubscriptionPort;
import com.aiott.ottpoc.config.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private static final Map<String, Integer> TIER_RANK = Map.of(
            "FREE", 0, "BASIC", 1, "PREMIUM", 2);

    private final StripeProperties stripeProps;
    private final UserSubscriptionPort subscriptionPort;

    @Value("${app.web.base-url:http://localhost:3000}")
    private String webBaseUrl;

    public PaymentService(StripeProperties stripeProps, UserSubscriptionPort subscriptionPort) {
        this.stripeProps = stripeProps;
        this.subscriptionPort = subscriptionPort;
        Stripe.apiKey = stripeProps.getSecretKey();
    }

    // ─── Checkout Session 생성 ──────────────────────────────────────────────

    /**
     * Stripe Hosted Checkout 세션을 생성하고 결제 페이지 URL을 반환합니다.
     *
     * @param userId    결제하는 사용자 ID
     * @param userEmail 사용자 이메일 (Stripe Customer 생성용)
     * @param plan      BASIC 또는 PREMIUM
     * @return Stripe 결제 페이지 URL
     */
    public String createCheckoutSession(UUID userId, String userEmail, String plan) {
        try {
            String priceId = getPriceId(plan);

            // Stripe Customer 생성 (결제 이력 추적용)
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(userEmail)
                            .putMetadata("userId", userId.toString())
                            .putMetadata("plan", plan)
                            .build());

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(webBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(webBaseUrl + "/pricing?cancelled=true")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("plan", plan)
                    .build();

            Session session = Session.create(params);
            log.info("[STRIPE] Checkout session created: sessionId={} userId={} plan={}", session.getId(), userId, plan);
            return session.getUrl();
        } catch (Exception e) {
            log.error("[STRIPE] Failed to create checkout session for userId={} plan={}: {}", userId, plan, e.getMessage(), e);
            throw new RuntimeException("결제 세션 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // ─── Stripe Portal (구독 관리) ─────────────────────────────────────────

    /**
     * Stripe Customer Portal URL을 생성합니다.
     * 사용자가 구독을 취소하거나 결제 수단을 변경할 수 있습니다.
     */
    public String createPortalSession(String stripeCustomerId) {
        try {
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(webBaseUrl + "/profile")
                            .build();

            com.stripe.model.billingportal.Session portalSession =
                    com.stripe.model.billingportal.Session.create(params);

            return portalSession.getUrl();
        } catch (Exception e) {
            log.error("[STRIPE] Failed to create portal session: {}", e.getMessage(), e);
            throw new RuntimeException("구독 관리 페이지를 열 수 없습니다: " + e.getMessage(), e);
        }
    }

    // ─── Webhook 처리 ──────────────────────────────────────────────────────

    /**
     * Stripe 웹훅을 검증하고 이벤트를 처리합니다.
     *
     * @param payload   원본 요청 바디 (서명 검증에 사용)
     * @param sigHeader Stripe-Signature 헤더 값
     */
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeProps.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("[STRIPE] Webhook signature verification failed: {}", e.getMessage());
            throw new SecurityException("Invalid Stripe webhook signature");
        }

        log.info("[STRIPE] Webhook event received: type={} id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.payment_failed" -> handlePaymentFailed(event);
            case "invoice.payment_succeeded" -> handlePaymentSucceeded(event);
            default -> log.debug("[STRIPE] Unhandled event type: {}", event.getType());
        }
    }

    // ─── 이벤트 핸들러 ────────────────────────────────────────────────────

    private void handleCheckoutCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            String userIdStr = session.getMetadata().get("userId");
            String plan = session.getMetadata().get("plan");
            String customerId = session.getCustomer();
            String subscriptionId = session.getSubscription();

            if (userIdStr == null || plan == null) {
                log.error("[STRIPE] Missing metadata in checkout.session.completed: {}", session.getId());
                return;
            }

            UUID userId = UUID.fromString(userIdStr);
            OffsetDateTime expiresAt = OffsetDateTime.now().plusMonths(1);

            subscriptionPort.updateSubscriptionTier(userId, plan.toUpperCase());
            subscriptionPort.saveStripeIds(userId, customerId, subscriptionId, expiresAt);

            log.info("[STRIPE] Subscription activated: userId={} plan={} customerId={}", userId, plan, customerId);
        } catch (Exception e) {
            log.error("[STRIPE] Error handling checkout.session.completed: {}", e.getMessage(), e);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        try {
            Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            String customerId = subscription.getCustomer();
            subscriptionPort.findUserIdByStripeCustomerId(customerId).ifPresent(userId -> {
                subscriptionPort.updateSubscriptionTier(userId, "FREE");
                log.info("[STRIPE] Subscription cancelled, downgraded to FREE: userId={}", userId);
            });
        } catch (Exception e) {
            log.error("[STRIPE] Error handling subscription.deleted: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentFailed(Event event) {
        // TODO: Phase 2 - 결제 실패 이메일 알림 전송
        log.warn("[STRIPE] Payment failed event received: {}", event.getId());
    }

    private void handlePaymentSucceeded(Event event) {
        // 갱신 결제 성공 시 만료일 연장
        try {
            com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            String customerId = invoice.getCustomer();
            String subscriptionId = invoice.getSubscription();

            subscriptionPort.findUserIdByStripeCustomerId(customerId).ifPresent(userId -> {
                OffsetDateTime newExpiry = OffsetDateTime.now().plusMonths(1);
                subscriptionPort.saveStripeIds(userId, customerId, subscriptionId, newExpiry);
                log.info("[STRIPE] Subscription renewed: userId={} newExpiry={}", userId, newExpiry);
            });
        } catch (Exception e) {
            log.error("[STRIPE] Error handling invoice.payment_succeeded: {}", e.getMessage(), e);
        }
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────────────────

    private String getPriceId(String plan) {
        return switch (plan.toUpperCase()) {
            case "BASIC" -> stripeProps.getBasicPriceId();
            case "PREMIUM" -> stripeProps.getPremiumPriceId();
            default -> throw new IllegalArgumentException("지원하지 않는 플랜: " + plan);
        };
    }
}
