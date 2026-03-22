package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserSummary(
        UUID id,
        String username,
        String role,
        String subscriptionTier,
        boolean emailVerified,
        OffsetDateTime createdAt,
        String stripeCustomerId,
        OffsetDateTime subscriptionExpiresAt
) {}
