package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.port.out.ContentAccessPort;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * ContentAccessPort JDBC 구현체.
 * JPA 엔티티 없이 단일 컬럼 조회만 수행하므로 JdbcTemplate을 직접 사용합니다.
 */
@Component
public class ContentAccessJdbcAdapter implements ContentAccessPort {

    private final JdbcTemplate jdbc;

    public ContentAccessJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> findRequiredTier(UUID contentId) {
        try {
            String tier = jdbc.queryForObject(
                    "SELECT required_tier FROM content WHERE id = ?",
                    String.class,
                    contentId);
            return Optional.ofNullable(tier);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
