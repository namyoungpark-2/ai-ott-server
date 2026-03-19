package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminUserSummary;
import com.aiott.ottpoc.application.port.in.AdminUserCommandUseCase;
import com.aiott.ottpoc.application.port.in.AdminUserQueryUseCase;
import com.aiott.ottpoc.application.port.out.admin.AdminUserCommandPort;
import com.aiott.ottpoc.application.port.out.admin.AdminUserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements AdminUserQueryUseCase, AdminUserCommandUseCase {

    private static final Set<String> VALID_TIERS = Set.of("FREE", "BASIC", "PREMIUM");

    private final AdminUserQueryPort queryPort;
    private final AdminUserCommandPort commandPort;

    @Override
    public List<AdminUserSummary> listUsers(String tierFilter) {
        if (tierFilter == null || tierFilter.isBlank()) {
            return queryPort.findAll();
        }
        String tier = tierFilter.toUpperCase();
        if (!VALID_TIERS.contains(tier)) {
            throw new IllegalArgumentException("Invalid tier: " + tierFilter);
        }
        return queryPort.findByTier(tier);
    }

    @Override
    @Transactional
    public void changeSubscriptionTier(UUID userId, String newTier) {
        String tier = newTier == null ? null : newTier.toUpperCase();
        if (!VALID_TIERS.contains(tier)) {
            throw new IllegalArgumentException("Invalid tier: " + newTier);
        }
        commandPort.updateSubscriptionTier(userId, tier);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        commandPort.deleteUser(userId);
    }
}
