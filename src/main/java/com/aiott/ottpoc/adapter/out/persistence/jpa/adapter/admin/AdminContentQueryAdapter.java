package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter.admin;

import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;
import com.aiott.ottpoc.application.port.out.admin.AdminContentQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminContentQueryAdapter implements AdminContentQueryPort {

    private final EntityManager em;

    @Override
    public List<AdminContentSummary> list(String lang, String status, int limit) {
        String s = (status == null || status.isBlank()) ? null : status.toUpperCase();
        int l = (limit <= 0 || limit > 200) ? 50 : limit;

        String sql = """
          select
            c.id as content_id,
            coalesce(ci.title, 'Untitled') as title,
            c.status as content_status,
            case
              when va.status = 'READY' then 'READY'
              when va.status = 'FAILED' then 'FAILED'
              else 'PROCESSING'
            end as ui_status,
            va.id as video_asset_id,
            va.status as video_asset_status,

            (select count(*) from transcoding_job tj where tj.video_asset_id = va.id) as attempt_count,
            (select tj2.status from transcoding_job tj2 where tj2.video_asset_id = va.id order by tj2.created_at desc limit 1) as latest_job_status,
            (select tj3.error_message from transcoding_job tj3 where tj3.video_asset_id = va.id order by tj3.created_at desc limit 1) as latest_error_message,

            case when va.id is not null then concat('/thumbnails/', va.id::text, '.jpg') else null end as thumbnail_url,
            case when va.status = 'READY' then concat('/hls/', va.id::text, '/master.m3u8') else null end as stream_url,

            c.created_at,
            c.updated_at
          from content c
          left join lateral (
            select ci1.title
            from content_i18n ci1
            where ci1.content_id = c.id
              and ci1.lang in (:lang, c.default_language, 'en')
            order by
              case
                when ci1.lang = :lang then 0
                when ci1.lang = c.default_language then 1
                else 2
              end
            limit 1
          ) ci on true
          left join lateral (
            select va1.*
            from video_asset va1
            where va1.content_id = c.id
            order by va1.created_at desc
            limit 1
          ) va on true
          where (cast(:status as text) is null or c.status = cast(:status as text))
          order by c.created_at desc
          limit :limit
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("lang", lang)
                .setParameter("status", s)
                .setParameter("limit", l)
                .getResultList();

        return rows.stream().map(r -> new AdminContentSummary(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (UUID) r[4],
                (String) r[5],
                r[6] == null ? 0L : ((Number) r[6]).longValue(),
                (String) r[7],
                (String) r[8],
                (String) r[9],
                (String) r[10],
                r[11] == null ? null : toOffsetDateTime(r[11]),
                r[12] == null ? null : toOffsetDateTime(r[12])
        )).toList();
    }

    @Override
    public Optional<AdminContentDetail> get(UUID contentId, String lang) {
        String sql = """
          select
            c.id as content_id,
            coalesce(ci.title, 'Untitled') as title,
            c.status as content_status,
            case
              when va.status = 'READY' then 'READY'
              when va.status = 'FAILED' then 'FAILED'
              else 'PROCESSING'
            end as ui_status,
            va.id as video_asset_id,
            va.status as video_asset_status,

            va.source_key,
            va.hls_master_key,
            va.error_message as video_asset_error_message,

            (select count(*) from transcoding_job tj where tj.video_asset_id = va.id) as attempt_count,
            (select tj2.status from transcoding_job tj2 where tj2.video_asset_id = va.id order by tj2.created_at desc limit 1) as latest_job_status,
            (select tj3.error_message from transcoding_job tj3 where tj3.video_asset_id = va.id order by tj3.created_at desc limit 1) as latest_error_message,

            case when va.id is not null then concat('/thumbnails/', va.id::text, '.jpg') else null end as thumbnail_url,
            case when va.status = 'READY' then concat('/hls/', va.id::text, '/master.m3u8') else null end as stream_url,

            c.created_at,
            c.updated_at
          from content c
          left join lateral (
            select ci1.title
            from content_i18n ci1
            where ci1.content_id = c.id
              and ci1.lang in (:lang, c.default_language, 'en')
            order by
              case
                when ci1.lang = :lang then 0
                when ci1.lang = c.default_language then 1
                else 2
              end
            limit 1
          ) ci on true
          left join lateral (
            select va1.*
            from video_asset va1
            where va1.content_id = c.id
            order by va1.created_at desc
            limit 1
          ) va on true
          where c.id = :contentId
        """;

        List<?> rows = em.createNativeQuery(sql)
                .setParameter("contentId", contentId)
                .setParameter("lang", lang)
                .getResultList();

        if (rows.isEmpty()) return Optional.empty();

        Object[] r = (Object[]) rows.get(0);
        return Optional.of(new AdminContentDetail(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (UUID) r[4],
                (String) r[5],
                (String) r[6],
                (String) r[7],
                (String) r[8],
                r[9] == null ? 0L : ((Number) r[9]).longValue(),
                (String) r[10],
                (String) r[11],
                (String) r[12],
                (String) r[13],
                r[14] == null ? null : toOffsetDateTime(r[14]),
                r[15] == null ? null : toOffsetDateTime(r[15])
        ));
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        }
        if (value instanceof Instant) {
            return OffsetDateTime.ofInstant((Instant) value, ZoneOffset.UTC);
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toInstant().atOffset(ZoneOffset.UTC);
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to OffsetDateTime");
    }
}
