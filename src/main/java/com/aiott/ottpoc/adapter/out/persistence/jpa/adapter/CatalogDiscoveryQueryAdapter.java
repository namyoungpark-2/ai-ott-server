package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.CatalogBrowseItemResult;
import com.aiott.ottpoc.application.dto.CatalogCategoryResult;
import com.aiott.ottpoc.application.dto.CatalogGenreResult;
import com.aiott.ottpoc.application.port.out.CatalogBrowseQueryPort;
import com.aiott.ottpoc.application.port.out.CatalogSearchQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogDiscoveryQueryAdapter implements CatalogBrowseQueryPort, CatalogSearchQueryPort {
    private final EntityManager em;

    @Override
    public List<CatalogBrowseItemResult> loadFeatured(String lang, int limit) {
        return loadItems(lang, "and c.is_featured = true", null, null, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> loadLatestMovies(String lang, int limit) {
        return loadItems(lang, "and c.content_type = 'MOVIE'", null, null, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> loadLatestSeriesEpisodes(String lang, int limit) {
        return loadItems(lang, "and c.content_type = 'EPISODE'", null, null, limit, 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CatalogCategoryResult> loadActiveCategories(String lang) {
        List<Object[]> rows = em.createNativeQuery("""
            select c.slug,
                   coalesce(ci.label, c.label) as label,
                   coalesce(ci.description, c.description) as description,
                   c.sort_order,
                   c.tier,
                   p.slug as parent_slug
            from category c
            left join category p on p.id = c.parent_id
            left join lateral (
                select ci1.label, ci1.description
                from category_i18n ci1
                where ci1.category_id = c.id
                  and ci1.lang in (:lang, c.default_language, 'en')
                order by case when ci1.lang = :lang then 0
                              when ci1.lang = c.default_language then 1 else 2 end
                limit 1
            ) ci on true
            where c.is_active = true
            order by c.tier asc, c.sort_order asc, coalesce(ci.label, c.label) asc
        """).setParameter("lang", lang).getResultList();
        return rows.stream().map(r -> new CatalogCategoryResult(
                (String) r[0], (String) r[1], (String) r[2],
                ((Number) r[3]).intValue(), ((Number) r[4]).intValue(), (String) r[5]
        )).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CatalogGenreResult> loadActiveGenres(String lang) {
        List<Object[]> rows = em.createNativeQuery("""
            select g.slug,
                   coalesce(gi.label, g.label) as label,
                   coalesce(gi.description, g.description) as description,
                   g.sort_order
            from genre g
            left join lateral (
                select gi1.label, gi1.description
                from genre_i18n gi1
                where gi1.genre_id = g.id
                  and gi1.lang in (:lang, g.default_language, 'en')
                order by case when gi1.lang = :lang then 0
                              when gi1.lang = g.default_language then 1 else 2 end
                limit 1
            ) gi on true
            where g.is_active = true
            order by g.sort_order asc, coalesce(gi.label, g.label) asc
        """).setParameter("lang", lang).getResultList();
        return rows.stream().map(r -> new CatalogGenreResult(
                (String) r[0], (String) r[1], (String) r[2], ((Number) r[3]).intValue()
        )).toList();
    }

    @Override
    public List<CatalogBrowseItemResult> loadByCategory(String lang, String categorySlug, int limit) {
        return loadItems(lang, "", categorySlug, null, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> loadByGenre(String lang, String genreSlug, int limit) {
        return loadItems(lang, "", null, genreSlug, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> search(String lang, String query, String category, String genre, int limit, int offset) {
        String normalized = query == null ? "" : query.trim();
        String filter = normalized.isBlank()
                ? ""
                : "and (lower(coalesce(ci.title, '')) like :q or lower(coalesce(ci.description, '')) like :q or exists (select 1 from content_tag ct2 join tag t2 on t2.id = ct2.tag_id where ct2.content_id = c.id and lower(t2.label) like :q))";
        return loadItems(lang, filter, category, genre, limit, offset, normalized);
    }

    @Override
    public int count(String lang, String query, String category, String genre) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        Object raw = em.createNativeQuery("""
            select count(*)
            from content c
            left join lateral (
              select ci1.title, ci1.description
              from content_i18n ci1
              where ci1.content_id = c.id
                and ci1.lang in (:lang, c.default_language, 'en')
              order by case when ci1.lang = :lang then 0 when ci1.lang = c.default_language then 1 else 2 end
              limit 1
            ) ci on true
            where c.status = 'PUBLISHED'
              and (cast(:category as text) is null or exists (
                select 1 from content_category cc join category cat on cat.id = cc.category_id
                where cc.content_id = c.id and cat.slug = cast(:category as text) and cat.is_active = true
              ))
              and (cast(:genre as text) is null or exists (
                select 1 from content_genre cg join genre g on g.id = cg.genre_id
                where cg.content_id = c.id and g.slug = cast(:genre as text) and g.is_active = true
              ))
              and (:q = '' or lower(coalesce(ci.title, '')) like :qlike or lower(coalesce(ci.description, '')) like :qlike or exists (
                select 1 from content_tag ct join tag t on t.id = ct.tag_id
                where ct.content_id = c.id and lower(t.label) like :qlike
              ))
        """)
                .setParameter("lang", lang)
                .setParameter("category", category)
                .setParameter("genre", genre)
                .setParameter("q", normalized)
                .setParameter("qlike", "%" + normalized + "%")
                .getSingleResult();
        return ((Number) raw).intValue();
    }

    // ── private ──────────────────────────────────────────────────

    private List<CatalogBrowseItemResult> loadItems(String lang, String additionalFilter,
                                                     String categorySlug, String genreSlug,
                                                     int limit, int offset) {
        return loadItems(lang, additionalFilter, categorySlug, genreSlug, limit, offset, null);
    }

    @SuppressWarnings("unchecked")
    private List<CatalogBrowseItemResult> loadItems(String lang, String additionalFilter,
                                                     String categorySlug, String genreSlug,
                                                     int limit, int offset, String query) {
        String sql = """
            select
              c.id,
              coalesce(ci.title, 'Untitled') as title,
              ci.description,
              c.content_type,
              c.status,
              coalesce(c.poster_url, concat('/thumbnails/', va.id::text, '.jpg')) as poster_url,
              c.banner_url,
              c.runtime_seconds,
              c.release_at,
              c.series_id,
              c.season_id,
              c.episode_number,
              coalesce(array_remove(array_agg(distinct cat.slug), null), '{}') as categories,
              coalesce(array_remove(array_agg(distinct coalesce(gi.label, g.label)), null), '{}') as genres,
              coalesce(array_remove(array_agg(distinct coalesce(ti.label, t.label)), null), '{}') as tags,
              va.video_width,
              va.video_height,
              va.duration_ms
            from content c
            left join lateral (
              select ci1.title, ci1.description
              from content_i18n ci1
              where ci1.content_id = c.id
                and ci1.lang in (:lang, c.default_language, 'en')
              order by case when ci1.lang = :lang then 0 when ci1.lang = c.default_language then 1 else 2 end
              limit 1
            ) ci on true
            left join lateral (
              select va1.* from video_asset va1 where va1.content_id = c.id order by va1.created_at desc limit 1
            ) va on true
            left join content_category cc on cc.content_id = c.id
            left join category cat on cat.id = cc.category_id and cat.is_active = true
            left join content_genre cg on cg.content_id = c.id
            left join genre g on g.id = cg.genre_id and g.is_active = true
            left join lateral (
              select gi1.label from genre_i18n gi1
              where gi1.genre_id = g.id
                and gi1.lang in (:lang, g.default_language, 'en')
              order by case when gi1.lang = :lang then 0 when gi1.lang = g.default_language then 1 else 2 end
              limit 1
            ) gi on true
            left join content_tag ct on ct.content_id = c.id
            left join tag t on t.id = ct.tag_id
            left join lateral (
              select ti1.label from tag_i18n ti1
              where ti1.tag_id = t.id
                and ti1.lang in (:lang, t.default_language, 'en')
              order by case when ti1.lang = :lang then 0 when ti1.lang = t.default_language then 1 else 2 end
              limit 1
            ) ti on true
            where c.status = 'PUBLISHED'
              and (cast(:category as text) is null or exists (
                select 1 from content_category cc2 join category cat2 on cat2.id = cc2.category_id
                where cc2.content_id = c.id and cat2.slug = cast(:category as text) and cat2.is_active = true
              ))
              and (cast(:genre as text) is null or exists (
                select 1 from content_genre cg2 join genre g2 on g2.id = cg2.genre_id
                where cg2.content_id = c.id and g2.slug = cast(:genre as text) and g2.is_active = true
              ))
        """ + additionalFilter + """
            group by c.id, ci.title, ci.description, va.id, va.video_width, va.video_height, va.duration_ms
            order by coalesce(c.release_at, now()) desc, c.created_at desc
            limit :limit offset :offset
        """;
        var nativeQuery = em.createNativeQuery(sql)
                .setParameter("lang", lang)
                .setParameter("category", categorySlug)
                .setParameter("genre", genreSlug)
                .setParameter("limit", limit)
                .setParameter("offset", offset);
        if (query != null) {
            nativeQuery.setParameter("q", "%" + query.toLowerCase() + "%");
        }
        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream().map(this::mapItem).toList();
    }

    private CatalogBrowseItemResult mapItem(Object[] r) {
        Integer videoWidth = r[15] == null ? null : ((Number) r[15]).intValue();
        Integer videoHeight = r[16] == null ? null : ((Number) r[16]).intValue();
        Long durationMs = r[17] == null ? null : ((Number) r[17]).longValue();
        return new CatalogBrowseItemResult(
                (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
                (String) r[4], (String) r[5], (String) r[6],
                r[7] == null ? null : ((Number) r[7]).intValue(),
                (OffsetDateTime) r[8], (UUID) r[9], (UUID) r[10],
                r[11] == null ? null : ((Number) r[11]).intValue(),
                toStringList(r[12]),  // categories
                toStringList(r[13]),  // genres
                toStringList(r[14]),  // tags
                videoWidth, videoHeight,
                resolveOrientation(videoWidth, videoHeight),
                durationMs
        );
    }

    private String resolveOrientation(Integer w, Integer h) {
        if (w == null || h == null || w == 0 || h == 0) return null;
        double ratio = (double) w / h;
        if (ratio >= 1.2) return "LANDSCAPE";
        if (ratio <= 0.8) return "PORTRAIT";
        return "SQUARE";
    }

    private List<String> toStringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof Array array) {
            try { return Arrays.asList((String[]) array.getArray()); }
            catch (Exception e) { return Collections.emptyList(); }
        }
        if (value instanceof String s) return List.of(s);
        return Collections.emptyList();
    }
}
