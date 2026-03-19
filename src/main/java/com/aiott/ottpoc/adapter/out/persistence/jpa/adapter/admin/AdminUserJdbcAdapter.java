package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter.admin;

import com.aiott.ottpoc.application.dto.admin.AdminUserSummary;
import com.aiott.ottpoc.application.port.out.admin.AdminUserCommandPort;
import com.aiott.ottpoc.application.port.out.admin.AdminUserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserJdbcAdapter implements AdminUserQueryPort, AdminUserCommandPort {

    private final JdbcTemplate jdbc;

    private static final String BASE_QUERY =
            """
            SELECT id, username, role, subscription_tier, email_verified,
                   created_at, stripe_customer_id, subscription_expires_at
            FROM users
            """;

    private final RowMapper<AdminUserSummary> rowMapper = (rs, rowNum) -> new AdminUserSummary(
            UUID.fromString(rs.getString("id")),
            rs.getString("username"),
            rs.getString("role"),
            rs.getString("subscription_tier"),
            rs.getBoolean("email_verified"),
            toOffsetDateTime(rs, "created_at"),
            rs.getString("stripe_customer_id"),
            toOffsetDateTime(rs, "subscription_expires_at")
    );

    @Override
    public List<AdminUserSummary> findAll() {
        return jdbc.query(BASE_QUERY + "ORDER BY created_at DESC", rowMapper);
    }

    @Override
    public List<AdminUserSummary> findByTier(String tier) {
        return jdbc.query(
                BASE_QUERY + "WHERE subscription_tier = ? ORDER BY created_at DESC",
                rowMapper, tier);
    }

    @Override
    public void updateSubscriptionTier(UUID userId, String tier) {
        jdbc.update("UPDATE users SET subscription_tier = ? WHERE id = ?", tier, userId);
    }

    @Override
    public void deleteUser(UUID userId) {
        // Delete in dependency order; ON DELETE CASCADE handles the rest
        jdbc.update("DELETE FROM user_watch_progress WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }

    private static OffsetDateTime toOffsetDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
