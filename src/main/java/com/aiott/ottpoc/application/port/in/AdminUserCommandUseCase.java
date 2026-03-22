package com.aiott.ottpoc.application.port.in;

import java.util.UUID;

public interface AdminUserCommandUseCase {

    /** Force-change a user's subscription tier (FREE / BASIC / PREMIUM). */
    void changeSubscriptionTier(UUID userId, String newTier);

    /** Hard-delete a user account and all associated data. */
    void deleteUser(UUID userId);
}
