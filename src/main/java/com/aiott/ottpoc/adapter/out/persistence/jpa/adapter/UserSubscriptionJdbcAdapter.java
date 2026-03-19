package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.port.out.UserSubscriptionPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserSubscriptionJdbcAdapter implements UserSubscriptionPort {

    private final JdbcTemplate jdbc;

    public UserSubscriptionJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void updateSubscriptionTier(UUID userId, String tier) {
        jdbc.update(
                "UPDATE users SET subscription_tier = ? WHERE id = ?",
                tier, userId);
    }

    @Override
    public void saveStripeIds(UUID userId, String customerId, String subscriptionId, OffsetDateTime expiresAt) {
        jdbc.update(
                """
                UPDATE users
                SET stripe_customer_id = ?,
                    stripe_subscription_id = ?,
                    subscription_expires_at = ?
                WHERE id = ?
                """,
                customerId, subscriptionId, expiresAt, userId);
    }

    @Override
    public Optional<UUID> findUserIdByStripeCustomerId(String customerId) {
        List<UUID> rows = jdbc.query(
                "SELECT id FROM users WHERE stripe_customer_id = ?",
                (rs, rowNum) -> UUID.fromString(rs.getString("id")),
                customerId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        List<UUID> rows = jdbc.query(
                "SELECT id FROM users WHERE username = ?",
                (rs, rowNum) -> UUID.fromString(rs.getString("id")),
                email);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
