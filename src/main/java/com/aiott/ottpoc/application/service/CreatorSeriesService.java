package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.channel.CreatorCreateSeriesCommand;
import com.aiott.ottpoc.application.dto.channel.CreatorSeriesResult;
import com.aiott.ottpoc.application.port.in.CreatorSeriesUseCase;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorSeriesService implements CreatorSeriesUseCase {
    private final CatalogCommandPort catalogCommandPort;
    private final ChannelQueryPort channelQueryPort;
    private final EntityManager em;

    @Override
    @Transactional
    public CreatorSeriesResult createSeries(String userId, CreatorCreateSeriesCommand cmd) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));

        UUID seriesId = catalogCommandPort.createSeriesWithChannel(cmd.title(), "en", channelId);

        if (cmd.description() != null) {
            em.createNativeQuery("""
                INSERT INTO series_i18n (series_id, lang, title, description)
                VALUES (:seriesId, 'en', :title, :description)
                ON CONFLICT (series_id, lang) DO UPDATE SET description = :description
            """)
            .setParameter("seriesId", seriesId)
            .setParameter("title", cmd.title())
            .setParameter("description", cmd.description())
            .executeUpdate();
        }

        return new CreatorSeriesResult(seriesId, cmd.title(), cmd.description(), 0);
    }

    @Override
    public List<CreatorSeriesResult> listMySeries(String userId, String lang) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid).orElse(null);
        if (channelId == null) return List.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT s.id, COALESCE(si.title, ''), COALESCE(si.description, ''),
                   (SELECT COUNT(*) FROM content c WHERE c.series_id = s.id)
            FROM series s
            LEFT JOIN series_i18n si ON si.series_id = s.id AND si.lang = :lang
            WHERE s.channel_id = :channelId
            ORDER BY s.created_at DESC
        """)
        .setParameter("channelId", channelId)
        .setParameter("lang", lang)
        .getResultList();

        return rows.stream().map(r -> new CreatorSeriesResult(
            (UUID) r[0], (String) r[1], (String) r[2], ((Number) r[3]).intValue()
        )).toList();
    }

    @Override
    @Transactional
    public void updateSeries(String userId, UUID seriesId, String title, String description, String lang) {
        verifySeriesOwnership(userId, seriesId);
        em.createNativeQuery("""
            INSERT INTO series_i18n (series_id, lang, title, description)
            VALUES (:seriesId, :lang, :title, :description)
            ON CONFLICT (series_id, lang) DO UPDATE SET title = :title, description = :description
        """)
        .setParameter("seriesId", seriesId)
        .setParameter("lang", lang)
        .setParameter("title", title)
        .setParameter("description", description)
        .executeUpdate();
    }

    private void verifySeriesOwnership(String userId, UUID seriesId) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));

        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
            "SELECT 1 FROM series WHERE id = :seriesId AND channel_id = :channelId")
            .setParameter("seriesId", seriesId)
            .setParameter("channelId", channelId)
            .getResultList();
        if (rows.isEmpty()) {
            throw new SecurityException("Not the owner of this series");
        }
    }
}
