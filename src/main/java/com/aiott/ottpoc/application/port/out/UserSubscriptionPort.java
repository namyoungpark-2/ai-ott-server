package com.aiott.ottpoc.application.port.out;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 구독/결제 관련 사용자 데이터 조작 포트.
 */
public interface UserSubscriptionPort {

    void updateSubscriptionTier(UUID userId, String tier);

    void saveStripeIds(UUID userId, String customerId, String subscriptionId, OffsetDateTime expiresAt);

    Optional<UUID> findUserIdByStripeCustomerId(String customerId);

    Optional<UUID> findUserIdByEmail(String email);
}
