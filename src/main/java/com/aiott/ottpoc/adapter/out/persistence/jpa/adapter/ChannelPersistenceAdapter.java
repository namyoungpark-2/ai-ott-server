package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.channel.*;
import com.aiott.ottpoc.application.port.out.ChannelCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ChannelPersistenceAdapter implements ChannelCommandPort, ChannelQueryPort {
    private final EntityManager em;

    @Override
    @Transactional
    public UUID createChannel(UUID ownerId, String handle, String name, String description) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery("""
            INSERT INTO channel (id, owner_id, handle, name, description, is_official, subscriber_count, status, created_at, updated_at)
            VALUES (:id, :ownerId, :handle, :name, :description, false, 0, 'ACTIVE', now(), now())
        """)
        .setParameter("id", id)
        .setParameter("ownerId", ownerId)
        .setParameter("handle", handle)
        .setParameter("name", name)
        .setParameter("description", description)
        .executeUpdate();
        return id;
    }

    @Override
    @Transactional
    public void updateChannel(UUID channelId, String name, String description, String profileImageUrl, String bannerImageUrl) {
        em.createNativeQuery("""
            UPDATE channel SET name = :name, description = :description,
                profile_image_url = :profileImageUrl, banner_image_url = :bannerImageUrl,
                updated_at = now()
            WHERE id = :channelId
        """)
        .setParameter("channelId", channelId)
        .setParameter("name", name)
        .setParameter("description", description)
        .setParameter("profileImageUrl", profileImageUrl)
        .setParameter("bannerImageUrl", bannerImageUrl)
        .executeUpdate();
    }

    @Override
    @Transactional
    public void updateChannelStatus(UUID channelId, String status) {
        em.createNativeQuery("UPDATE channel SET status = :status, updated_at = now() WHERE id = :channelId")
        .setParameter("channelId", channelId)
        .setParameter("status", status)
        .executeUpdate();
    }

    @Override
    @Transactional
    public void incrementSubscriberCount(UUID channelId) {
        em.createNativeQuery("UPDATE channel SET subscriber_count = subscriber_count + 1, updated_at = now() WHERE id = :channelId")
        .setParameter("channelId", channelId)
        .executeUpdate();
    }

    @Override
    @Transactional
    public void decrementSubscriberCount(UUID channelId) {
        em.createNativeQuery("UPDATE channel SET subscriber_count = GREATEST(subscriber_count - 1, 0), updated_at = now() WHERE id = :channelId")
        .setParameter("channelId", channelId)
        .executeUpdate();
    }

    @Override
    public Optional<ChannelDetailResult> findByHandle(String handle, String lang) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT c.id, c.handle, COALESCE(ci.name, c.name), COALESCE(ci.description, c.description),
                   c.profile_image_url, c.banner_image_url, c.is_official, c.subscriber_count, c.status, c.created_at
            FROM channel c
            LEFT JOIN channel_i18n ci ON ci.channel_id = c.id AND ci.lang = :lang
            WHERE c.handle = :handle
        """)
        .setParameter("handle", handle)
        .setParameter("lang", lang)
        .getResultList();

        if (rows.isEmpty()) return Optional.empty();
        Object[] r = rows.get(0);
        return Optional.of(new ChannelDetailResult(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (String) r[4], (String) r[5], (Boolean) r[6], ((Number) r[7]).intValue(),
            (String) r[8], toOffsetDateTime(r[9])
        ));
    }

    @Override
    public Optional<ChannelDetailResult> findByOwnerId(UUID ownerId, String lang) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT c.id, c.handle, COALESCE(ci.name, c.name), COALESCE(ci.description, c.description),
                   c.profile_image_url, c.banner_image_url, c.is_official, c.subscriber_count, c.status, c.created_at
            FROM channel c
            LEFT JOIN channel_i18n ci ON ci.channel_id = c.id AND ci.lang = :lang
            WHERE c.owner_id = :ownerId
        """)
        .setParameter("ownerId", ownerId)
        .setParameter("lang", lang)
        .getResultList();

        if (rows.isEmpty()) return Optional.empty();
        Object[] r = rows.get(0);
        return Optional.of(new ChannelDetailResult(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (String) r[4], (String) r[5], (Boolean) r[6], ((Number) r[7]).intValue(),
            (String) r[8], toOffsetDateTime(r[9])
        ));
    }

    @Override
    public Optional<UUID> findChannelIdByOwnerId(UUID ownerId) {
        @SuppressWarnings("unchecked")
        List<UUID> rows = em.createNativeQuery("SELECT id FROM channel WHERE owner_id = :ownerId")
            .setParameter("ownerId", ownerId)
            .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public Optional<UUID> findOfficialChannelId() {
        @SuppressWarnings("unchecked")
        List<UUID> rows = em.createNativeQuery("SELECT id FROM channel WHERE is_official = true LIMIT 1")
            .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<ChannelSummaryResult> listAll(int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT id, handle, name, profile_image_url, is_official, subscriber_count, status
            FROM channel ORDER BY created_at DESC LIMIT :limit
        """)
        .setParameter("limit", limit)
        .getResultList();

        return rows.stream().map(r -> new ChannelSummaryResult(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (Boolean) r[4], ((Number) r[5]).intValue(), (String) r[6]
        )).toList();
    }

    @Override
    public List<ChannelContentResult> listContentsByChannelHandle(String handle, String lang, int limit, int offset) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT co.id, COALESCE(ci.title, ''), co.content_type, co.status, co.poster_url, co.created_at
            FROM content co
            JOIN channel ch ON ch.id = co.channel_id
            LEFT JOIN content_i18n ci ON ci.content_id = co.id AND ci.lang = :lang
            WHERE ch.handle = :handle AND co.status = 'PUBLISHED'
            ORDER BY co.created_at DESC
            LIMIT :limit OFFSET :offset
        """)
        .setParameter("handle", handle)
        .setParameter("lang", lang)
        .setParameter("limit", limit)
        .setParameter("offset", offset)
        .getResultList();

        return rows.stream().map(r -> new ChannelContentResult(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (String) r[4], toOffsetDateTime(r[5])
        )).toList();
    }

    @Override
    public List<ChannelSeriesResult> listSeriesByChannelHandle(String handle, String lang) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT s.id, COALESCE(si.title, ''), s.status,
                   (SELECT COUNT(*) FROM content c WHERE c.series_id = s.id)
            FROM series s
            JOIN channel ch ON ch.id = s.channel_id
            LEFT JOIN series_i18n si ON si.series_id = s.id AND si.lang = :lang
            WHERE ch.handle = :handle AND s.status = 'PUBLISHED'
            ORDER BY s.created_at DESC
        """)
        .setParameter("handle", handle)
        .setParameter("lang", lang)
        .getResultList();

        return rows.stream().map(r -> new ChannelSeriesResult(
            (UUID) r[0], (String) r[1], (String) r[2], ((Number) r[3]).intValue()
        )).toList();
    }

    private OffsetDateTime toOffsetDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof OffsetDateTime odt) return odt;
        if (val instanceof Instant inst) return inst.atOffset(ZoneOffset.UTC);
        if (val instanceof java.sql.Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        return null;
    }
}
