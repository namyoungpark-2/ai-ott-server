package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentMetadataCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentTaxonomyCommand;
import com.aiott.ottpoc.application.port.out.AdminCategoryCommandPort;
import com.aiott.ottpoc.application.port.out.AdminCategoryQueryPort;
import com.aiott.ottpoc.application.port.out.AdminContentMetadataCommandPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminTaxonomyPersistenceAdapter
        implements AdminCategoryCommandPort, AdminCategoryQueryPort,
                   AdminContentMetadataCommandPort {

    private final EntityManager em;

    // ── Category ─────────────────────────────────────────────────

    @Override
    public AdminCategoryResult create(AdminCreateCategoryCommand command) {
        UUID id = UUID.randomUUID();
        String slug = normalizeSlug(command.slug() == null || command.slug().isBlank() ? command.label() : command.slug());
        String lang = command.lang() == null || command.lang().isBlank() ? "en" : command.lang();
        int sortOrder = command.sortOrder() == null ? 0 : command.sortOrder();
        boolean active = command.active() == null || command.active();
        int tier = command.tier() == null ? 1 : command.tier();

        // parent_id 조회 (parentSlug 기반)
        UUID parentId = null;
        if (command.parentSlug() != null && !command.parentSlug().isBlank()) {
            var result = em.createNativeQuery("select id from category where slug = :slug")
                    .setParameter("slug", command.parentSlug())
                    .getResultList();
            if (!result.isEmpty()) parentId = (UUID) result.get(0);
        }

        em.createNativeQuery("""
            insert into category (id, slug, label, description, sort_order, is_active, default_language, iab_code, tier, parent_id, created_at, updated_at)
            values (:id, :slug, :label, :description, :sortOrder, :active, :lang, :iabCode, :tier, :parentId, now(), now())
        """)
                .setParameter("id", id).setParameter("slug", slug)
                .setParameter("label", command.label()).setParameter("description", command.description())
                .setParameter("sortOrder", sortOrder).setParameter("active", active).setParameter("lang", lang)
                .setParameter("iabCode", command.iabCode()).setParameter("tier", tier).setParameter("parentId", parentId)
                .executeUpdate();

        em.createNativeQuery("""
            insert into category_i18n (category_id, lang, label, description, created_at, updated_at)
            values (:categoryId, :lang, :label, :description, now(), now())
            on conflict (category_id, lang) do update
                set label = excluded.label, description = excluded.description, updated_at = now()
        """)
                .setParameter("categoryId", id).setParameter("lang", lang)
                .setParameter("label", command.label()).setParameter("description", command.description())
                .executeUpdate();

        return new AdminCategoryResult(id, slug, command.label(), command.description(), sortOrder, active,
                command.iabCode(), tier, parentId);
    }

    @Override
    public AdminCategoryResult update(String slug, AdminUpdateCategoryCommand command) {
        // find existing category
        @SuppressWarnings("unchecked")
        List<Object[]> existing = em.createNativeQuery(
                "select id, default_language from category where slug = :slug")
                .setParameter("slug", slug)
                .getResultList();
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Category not found: " + slug);
        }
        UUID id = (UUID) existing.get(0)[0];
        String lang = (String) existing.get(0)[1];

        // resolve parentId
        UUID parentId = null;
        if (command.parentSlug() != null && !command.parentSlug().isBlank()) {
            @SuppressWarnings("unchecked")
            var result = em.createNativeQuery("select id from category where slug = :slug")
                    .setParameter("slug", command.parentSlug())
                    .getResultList();
            if (!result.isEmpty()) parentId = (UUID) result.get(0);
        }

        int sortOrder = command.sortOrder() == null ? 0 : command.sortOrder();
        boolean active = command.active() == null || command.active();
        int tier = command.tier() == null ? 1 : command.tier();

        em.createNativeQuery("""
            update category
            set label = :label, description = :description, sort_order = :sortOrder,
                is_active = :active, iab_code = :iabCode, tier = :tier,
                parent_id = :parentId, updated_at = now()
            where id = :id
        """)
                .setParameter("label", command.label())
                .setParameter("description", command.description())
                .setParameter("sortOrder", sortOrder)
                .setParameter("active", active)
                .setParameter("iabCode", command.iabCode())
                .setParameter("tier", tier)
                .setParameter("parentId", parentId)
                .setParameter("id", id)
                .executeUpdate();

        // upsert i18n
        em.createNativeQuery("""
            insert into category_i18n (category_id, lang, label, description, created_at, updated_at)
            values (:categoryId, :lang, :label, :description, now(), now())
            on conflict (category_id, lang) do update
                set label = excluded.label, description = excluded.description, updated_at = now()
        """)
                .setParameter("categoryId", id).setParameter("lang", lang)
                .setParameter("label", command.label()).setParameter("description", command.description())
                .executeUpdate();

        return new AdminCategoryResult(id, slug, command.label(), command.description(),
                sortOrder, active, command.iabCode(), tier, parentId);
    }

    @Override
    public void delete(String slug) {
        @SuppressWarnings("unchecked")
        var result = em.createNativeQuery("select id from category where slug = :slug")
                .setParameter("slug", slug)
                .getResultList();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Category not found: " + slug);
        }
        UUID id = (UUID) result.get(0);

        em.createNativeQuery("delete from content_category where category_id = :id")
                .setParameter("id", id).executeUpdate();
        em.createNativeQuery("delete from category_i18n where category_id = :id")
                .setParameter("id", id).executeUpdate();
        // 자식 카테고리의 parent_id를 null로 변경
        em.createNativeQuery("update category set parent_id = null where parent_id = :id")
                .setParameter("id", id).executeUpdate();
        em.createNativeQuery("delete from category where id = :id")
                .setParameter("id", id).executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AdminCategoryResult> list() {
        List<Object[]> rows = em.createNativeQuery("""
            select c.id, c.slug, coalesce(ci.label, c.label), coalesce(ci.description, c.description),
                   c.sort_order, c.is_active, c.iab_code, c.tier, c.parent_id
            from category c
            left join category_i18n ci on ci.category_id = c.id and ci.lang = c.default_language
            order by c.tier asc, c.sort_order asc, coalesce(ci.label, c.label) asc
        """).getResultList();
        return rows.stream().map(r -> new AdminCategoryResult(
                (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
                ((Number) r[4]).intValue(), (Boolean) r[5],
                (String) r[6], ((Number) r[7]).intValue(), (UUID) r[8]
        )).toList();
    }

    // ── Content Metadata ─────────────────────────────────────────

    @Override
    public void updateMetadata(UUID contentId, AdminUpdateContentMetadataCommand command) {
        String lang = command.lang() == null || command.lang().isBlank() ? "en" : command.lang();
        em.createNativeQuery("""
            update content
            set runtime_seconds = coalesce(:runtimeSeconds, runtime_seconds),
                release_at = coalesce(:releaseAt, release_at),
                poster_url = coalesce(:posterUrl, poster_url),
                banner_url = coalesce(:bannerUrl, banner_url),
                age_rating = coalesce(:ageRating, age_rating),
                is_featured = coalesce(:featured, is_featured),
                status = coalesce(:status, status),
                updated_at = now()
            where id = :contentId
        """)
                .setParameter("runtimeSeconds", command.runtimeSeconds())
                .setParameter("releaseAt", command.releaseAt())
                .setParameter("posterUrl", command.posterUrl())
                .setParameter("bannerUrl", command.bannerUrl())
                .setParameter("ageRating", command.ageRating())
                .setParameter("featured", command.featured())
                .setParameter("status", command.status())
                .setParameter("contentId", contentId)
                .executeUpdate();
        if (command.title() != null || command.description() != null) {
            em.createNativeQuery("""
                insert into content_i18n (content_id, lang, title, description, created_at, updated_at)
                values (:contentId, :lang, coalesce(:title, 'Untitled'), :description, now(), now())
                on conflict (content_id, lang)
                do update set title = coalesce(excluded.title, content_i18n.title),
                              description = coalesce(excluded.description, content_i18n.description),
                              updated_at = now()
            """)
                    .setParameter("contentId", contentId)
                    .setParameter("lang", lang)
                    .setParameter("title", command.title())
                    .setParameter("description", command.description())
                    .executeUpdate();
        }
    }

    // ── Content Taxonomy ─────────────────────────────────────────

    @Override
    public void updateTaxonomy(UUID contentId, AdminUpdateContentTaxonomyCommand command) {
        String lang = command.lang() == null ? "en" : command.lang();

        // categories
        em.createNativeQuery("delete from content_category where content_id = :contentId")
                .setParameter("contentId", contentId).executeUpdate();
        if (command.categorySlugs() != null) {
            for (String slug : command.categorySlugs()) {
                em.createNativeQuery("""
                    insert into content_category (content_id, category_id, created_at)
                    select :contentId, id, now() from category where slug = :slug
                    on conflict do nothing
                """).setParameter("contentId", contentId).setParameter("slug", slug).executeUpdate();
            }
        }

        // genres
        em.createNativeQuery("delete from content_genre where content_id = :contentId")
                .setParameter("contentId", contentId).executeUpdate();
        if (command.genreSlugs() != null) {
            for (String slug : command.genreSlugs()) {
                em.createNativeQuery("""
                    insert into content_genre (content_id, genre_id, created_at)
                    select :contentId, id, now() from genre where slug = :slug
                    on conflict do nothing
                """).setParameter("contentId", contentId).setParameter("slug", slug).executeUpdate();
            }
        }

        // tags
        em.createNativeQuery("delete from content_tag where content_id = :contentId")
                .setParameter("contentId", contentId).executeUpdate();
        if (command.tags() != null) {
            for (String raw : command.tags()) {
                String slug = normalizeSlug(raw);
                em.createNativeQuery("""
                    insert into tag (id, slug, label, default_language, created_at, updated_at)
                    values (:id, :slug, :label, :lang, now(), now())
                    on conflict (slug) do update set label = excluded.label, updated_at = now()
                """).setParameter("id", UUID.randomUUID()).setParameter("slug", slug)
                        .setParameter("label", raw).setParameter("lang", lang).executeUpdate();
                em.createNativeQuery("""
                    insert into tag_i18n (tag_id, lang, label, created_at, updated_at)
                    select t.id, :lang, :label, now(), now() from tag t where t.slug = :slug
                    on conflict (tag_id, lang) do update set label = excluded.label, updated_at = now()
                """).setParameter("slug", slug).setParameter("lang", lang).setParameter("label", raw).executeUpdate();
                em.createNativeQuery("""
                    insert into content_tag (content_id, tag_id, created_at)
                    select :contentId, id, now() from tag where slug = :slug
                    on conflict do nothing
                """).setParameter("contentId", contentId).setParameter("slug", slug).executeUpdate();
            }
        }
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
