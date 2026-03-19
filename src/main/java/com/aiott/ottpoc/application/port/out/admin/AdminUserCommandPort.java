package com.aiott.ottpoc.application.port.out.admin;

import java.util.UUID;

public interface AdminUserCommandPort {

    void updateSubscriptionTier(UUID userId, String tier);

    void deleteUser(UUID userId);
}
