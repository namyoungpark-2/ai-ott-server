package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.WatchProgressItem;
import com.aiott.ottpoc.application.port.out.UserWatchProgressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserWatchProgressJdbcAdapter implements UserWatchProgressPort {

    private final JdbcTemplate jdbc;

    @Override
    public List<WatchProgressItem> findContinueWatching(UUID userId, String lang, int limit) {
        String sql = """
                SELECT
                    uwp.content_id,
                    COALESCE(ci.title, 'Untitled') AS title,
                    CASE WHEN va.id IS NOT NULL
                         THEN CONCAT('/thumbnails/', va.id::text, '.jpg')
                         ELSE NULL
                    END AS thumbnail_url,
                    uwp.position_ms,
                    uwp.duration_ms,
                    va.video_width,
                    va.video_height
                FROM user_watch_progress uwp
                JOIN content c ON c.id = uwp.content_id AND c.status IN ('DRAFT','PUBLISHED')
                LEFT JOIN LATERAL (
                    SELECT ci1.title
                    FROM content_i18n ci1
                    WHERE ci1.content_id = c.id
                      AND ci1.lang IN (?, c.default_language, 'en')
                    ORDER BY
                        CASE
                            WHEN ci1.lang = ?                  THEN 0
                            WHEN ci1.lang = c.default_language THEN 1
                            ELSE 2
                        END
                    LIMIT 1
                ) ci ON true
                LEFT JOIN LATERAL (
                    SELECT va1.id, va1.video_width, va1.video_height
                    FROM video_asset va1
                    WHERE va1.content_id = c.id AND va1.status = 'READY'
                    ORDER BY va1.created_at DESC
                    LIMIT 1
                ) va ON true
                WHERE uwp.user_id = ?
                  AND uwp.position_ms > 0
                  AND (uwp.duration_ms IS NULL OR uwp.position_ms < uwp.duration_ms * 0.95)
                ORDER BY uwp.updated_at DESC
                LIMIT ?
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            long posMs = rs.getLong("position_ms");
            long durMs = rs.getLong("duration_ms");
            boolean hasDuration = !rs.wasNull() && durMs > 0;
            Integer pct = hasDuration ? (int) Math.min(100, posMs * 100 / durMs) : null;
            int vw = rs.getInt("video_width");
            Integer videoWidth = rs.wasNull() ? null : vw;
            int vh = rs.getInt("video_height");
            Integer videoHeight = rs.wasNull() ? null : vh;
            return new WatchProgressItem(
                    UUID.fromString(rs.getString("content_id")),
                    rs.getString("title"),
                    rs.getString("thumbnail_url"),
                    posMs,
                    hasDuration ? durMs : null,
                    pct,
                    videoWidth,
                    videoHeight,
                    resolveOrientation(videoWidth, videoHeight)
            );
        }, lang, lang, userId, limit);
    }

    @Override
    public long findPosition(UUID userId, UUID contentId) {
        List<Long> rows = jdbc.query(
                "SELECT position_ms FROM user_watch_progress WHERE user_id = ? AND content_id = ?",
                (rs, rowNum) -> rs.getLong("position_ms"),
                userId, contentId);
        return rows.isEmpty() ? 0L : rows.get(0);
    }

    private String resolveOrientation(Integer w, Integer h) {
        if (w == null || h == null || w == 0 || h == 0) return null;
        double ratio = (double) w / h;
        if (ratio >= 1.2) return "LANDSCAPE";
        if (ratio <= 0.8) return "PORTRAIT";
        return "SQUARE";
    }

    @Override
    public void upsertProgress(UUID userId, UUID contentId, long positionMs, Long durationMs) {
        jdbc.update("""
                INSERT INTO user_watch_progress (user_id, content_id, position_ms, duration_ms, updated_at)
                VALUES (?, ?, ?, ?, NOW())
                ON CONFLICT (user_id, content_id)
                DO UPDATE SET position_ms = EXCLUDED.position_ms,
                              duration_ms = COALESCE(EXCLUDED.duration_ms, user_watch_progress.duration_ms),
                              updated_at  = NOW()
                """,
                userId, contentId, positionMs, durationMs);
    }
}
