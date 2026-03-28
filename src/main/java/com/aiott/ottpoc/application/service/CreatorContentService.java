package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminAttachAssetResult;
import com.aiott.ottpoc.application.dto.channel.CreatorContentResult;
import com.aiott.ottpoc.application.dto.channel.CreatorContentSummary;
import com.aiott.ottpoc.application.dto.channel.CreatorCreateContentCommand;
import com.aiott.ottpoc.application.port.in.CreatorContentUseCase;
import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorContentService implements CreatorContentUseCase {
    private final CatalogCommandPort catalogCommandPort;
    private final ChannelQueryPort channelQueryPort;
    private final EntityManager em;
    private final AssetStoragePort storagePort;
    private final MediaStoragePort mediaStoragePort;
    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase;

    @Override
    @Transactional
    public CreatorContentResult createContent(String userId, CreatorCreateContentCommand cmd) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid)
                .orElseThrow(() -> new IllegalStateException("Channel not found. Create channel first."));

        String mode = cmd.mode() == null ? "MOVIE" : cmd.mode().toUpperCase();
        String title = cmd.title() == null || cmd.title().isBlank() ? "Untitled" : cmd.title();

        UUID contentId;
        if ("EPISODE".equals(mode)) {
            UUID seriesId = cmd.seriesId();
            if (seriesId == null && cmd.seriesTitle() != null) {
                seriesId = catalogCommandPort.createSeriesWithChannel(cmd.seriesTitle(), "en", channelId);
            }
            if (seriesId == null) {
                throw new IllegalArgumentException("seriesId or seriesTitle required for EPISODE");
            }
            int seasonNum = cmd.seasonNumber() == null ? 1 : cmd.seasonNumber();
            UUID seasonId = catalogCommandPort.ensureSeason(seriesId, seasonNum, "en");
            int epNum = cmd.episodeNumber() == null ? catalogCommandPort.nextEpisodeNumber(seasonId) : cmd.episodeNumber();
            contentId = catalogCommandPort.createEpisodeContent(seriesId, seasonId, epNum, title);
        } else {
            contentId = catalogCommandPort.createMovieContentWithChannel(title, channelId);
        }

        // Set channel_id for episode content too
        if ("EPISODE".equals(mode)) {
            em.createNativeQuery("UPDATE content SET channel_id = :channelId WHERE id = :contentId")
                .setParameter("channelId", channelId)
                .setParameter("contentId", contentId)
                .executeUpdate();
        }

        // Auto-publish for initial phase
        catalogCommandPort.updateContentStatus(contentId, "PUBLISHED");

        return new CreatorContentResult(contentId);
    }

    @Override
    public List<CreatorContentSummary> listMyContents(String userId, String lang, int limit) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid).orElse(null);
        if (channelId == null) return List.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT co.id, COALESCE(ci.title, 'Untitled'), co.content_type, co.status,
                   (SELECT va.status FROM video_asset va WHERE va.content_id = co.id ORDER BY va.created_at DESC LIMIT 1),
                   co.poster_url, co.created_at
            FROM content co
            LEFT JOIN content_i18n ci ON ci.content_id = co.id AND ci.lang = :lang
            WHERE co.channel_id = :channelId
            ORDER BY co.created_at DESC
            LIMIT :limit
        """)
        .setParameter("channelId", channelId)
        .setParameter("lang", lang)
        .setParameter("limit", limit)
        .getResultList();

        return rows.stream().map(r -> new CreatorContentSummary(
            (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
            (String) r[4], (String) r[5],
            r[6] != null ? toOffsetDateTime(r[6]) : null
        )).toList();
    }

    @Override
    @Transactional
    public void updateContentMetadata(String userId, UUID contentId, String title, String description, String lang) {
        verifyOwnership(userId, contentId);
        em.createNativeQuery("""
            INSERT INTO content_i18n (content_id, lang, title, description, created_at, updated_at)
            VALUES (:contentId, :lang, :title, :description, now(), now())
            ON CONFLICT (content_id, lang) DO UPDATE SET title = :title, description = :description, updated_at = now()
        """)
        .setParameter("contentId", contentId)
        .setParameter("lang", lang)
        .setParameter("title", title)
        .setParameter("description", description)
        .executeUpdate();
    }

    @Override
    @Transactional
    public void updateContentStatus(String userId, UUID contentId, String status) {
        verifyOwnership(userId, contentId);
        catalogCommandPort.updateContentStatus(contentId, status);
    }

    @Override
    @Transactional
    public void deleteContent(String userId, UUID contentId) {
        verifyOwnership(userId, contentId);
        // Soft delete by archiving
        catalogCommandPort.updateContentStatus(contentId, "ARCHIVED");
    }

    private OffsetDateTime toOffsetDateTime(Object val) {
        if (val instanceof OffsetDateTime odt) return odt;
        if (val instanceof java.time.Instant inst) return inst.atOffset(ZoneOffset.UTC);
        if (val instanceof java.sql.Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        return null;
    }

    @Override
    @Transactional
    public AdminAttachAssetResult uploadAsset(String userId, UUID contentId, MultipartFile file) {
        verifyOwnership(userId, contentId);
        try {
            var tempPath = storagePort.saveSourceVideo(file.getBytes(), file.getOriginalFilename());
            var sourceKey = mediaStoragePort.storeSource(tempPath, contentId, file.getOriginalFilename());
            var videoAssetId = videoAssetCommandPort.createVideoAsset(
                    contentId, mediaStoragePort.storageType(), sourceKey, VideoAssetStatus.UPLOADED);
            transcodeVideoAssetUseCase.transcode(videoAssetId, null);
            return new AdminAttachAssetResult(contentId, videoAssetId, "PROCESSING");
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private void verifyOwnership(String userId, UUID contentId) {
        UUID uid = UUID.fromString(userId);
        UUID channelId = channelQueryPort.findChannelIdByOwnerId(uid)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));

        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
            "SELECT 1 FROM content WHERE id = :contentId AND channel_id = :channelId")
            .setParameter("contentId", contentId)
            .setParameter("channelId", channelId)
            .getResultList();
        if (rows.isEmpty()) {
            throw new SecurityException("Not the owner of this content");
        }
    }
}
