package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.CatalogBrowseItemResult;
import com.aiott.ottpoc.application.dto.CatalogCategoryResult;
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
        return loadItems(lang, "and c.is_featured = true", null, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> loadLatestMovies(String lang, int limit) {
        return loadItems(lang, "and c.content_type = 'MOVIE'", null, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> loadLatestSeriesEpisodes(String lang, int limit) {
        return loadItems(lang, "and c.content_type = 'EPISODE'", null, limit, 0);
    }

    @Override
    public List<CatalogCategoryResult> loadActiveCategories() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            select slug, label, description, sort_order
            from category
            where is_active = true
            order by sort_order asc, label asc
        """).getResultList();
        return rows.stream().map(r -> new CatalogCategoryResult(
                (String) r[0],
                (String) r[1],
                (String) r[2],
                ((Number) r[3]).intValue()
        )).toList();
    }

    @Override
    public List<CatalogBrowseItemResult> loadByCategory(String lang, String categorySlug, int limit) {
        return loadItems(lang, "", categorySlug, limit, 0);
    }

    @Override
    public List<CatalogBrowseItemResult> search(String lang, String query, String category, int limit, int offset) {
        String normalized = query == null ? "" : query.trim();
        String filter = normalized.isBlank()
                ? ""
                : "and (lower(coalesce(ci.title, '')) like :q or lower(coalesce(ci.description, '')) like :q or exists (select 1 from content_tag ct join tag t on t.id = ct.tag_id where ct.content_id = c.id and lower(t.label) like :q))";
        return loadItems(lang, filter, category, limit, offset, normalized);
    }

    @Override
    public int count(String lang, String query, String category) {
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
              and (:category is null or exists (
                select 1 from content_category cc join category cat on cat.id = cc.category_id
                where cc.content_id = c.id and cat.slug = :category and cat.is_active = true
              ))
              and (:q = '' or lower(coalesce(ci.title, '')) like :qlike or lower(coalesce(ci.description, '')) like :qlike or exists (
                select 1 from content_tag ct join tag t on t.id = ct.tag_id
                where ct.content_id = c.id and lower(t.label) like :qlike
              ))
        """)
                .setParameter("lang", lang)
                .setParameter("category", category)
                .setParameter("q", normalized)
                .setParameter("qlike", "%" + normalized + "%")
                .getSingleResult();
        return ((Number) raw).intValue();
    }

    private List<CatalogBrowseItemResult> loadItems(String lang, String additionalFilter, String categorySlug, int limit, int offset) {
        return loadItems(lang, additionalFilter, categorySlug, limit, offset, null);
    }

    @SuppressWarnings("unchecked")
    private List<CatalogBrowseItemResult> loadItems(String lang, String additionalFilter, String categorySlug, int limit, int offset, String query) {
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
              coalesce(array_remove(array_agg(distinct t.label), null), '{}') as tags
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
            left join content_tag ct on ct.content_id = c.id
            left join tag t on t.id = ct.tag_id
            where c.status = 'PUBLISHED'
              and (:category is null or exists (
                select 1 from content_category cc2 join category cat2 on cat2.id = cc2.category_id
                where cc2.content_id = c.id and cat2.slug = :category and cat2.is_active = true
              ))
        """ + additionalFilter + """
            group by c.id, ci.title, ci.description, va.id
            order by coalesce(c.release_at, now()) desc, c.created_at desc
            limit :limit offset :offset
        """;
        var nativeQuery = em.createNativeQuery(sql)
                .setParameter("lang", lang)
                .setParameter("category", categorySlug)
                .setParameter("limit", limit)
                .setParameter("offset", offset);
        if (query != null) {
            nativeQuery.setParameter("q", "%" + query.toLowerCase() + "%");
        }
        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream().map(this::mapItem).toList();
    }

    private CatalogBrowseItemResult mapItem(Object[] r) {
        return new CatalogBrowseItemResult(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4],
                (String) r[5],
                (String) r[6],
                r[7] == null ? null : ((Number) r[7]).intValue(),
                (OffsetDateTime) r[8],
                (UUID) r[9],
                (UUID) r[10],
                r[11] == null ? null : ((Number) r[11]).intValue(),
                toStringList(r[12]),
                toStringList(r[13])
        );
    }

    private List<String> toStringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof Array array) {
            try {
                return Arrays.asList((String[]) array.getArray());
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        if (value instanceof String s) {
            return List.of(s);
        }
        return Collections.emptyList();
    }
}
