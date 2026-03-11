package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
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
public class AdminTaxonomyPersistenceAdapter implements AdminCategoryCommandPort, AdminCategoryQueryPort, AdminContentMetadataCommandPort {
    private final EntityManager em;

    @Override
    public AdminCategoryResult create(AdminCreateCategoryCommand command) {
        UUID id = UUID.randomUUID();
        String slug = normalizeSlug(command.slug() == null || command.slug().isBlank() ? command.label() : command.slug());
        em.createNativeQuery("""
            insert into category (id, slug, label, description, sort_order, is_active, created_at, updated_at)
            values (:id, :slug, :label, :description, :sortOrder, :active, now(), now())
        """)
                .setParameter("id", id)
                .setParameter("slug", slug)
                .setParameter("label", command.label())
                .setParameter("description", command.description())
                .setParameter("sortOrder", command.sortOrder() == null ? 0 : command.sortOrder())
                .setParameter("active", command.active() == null || command.active())
                .executeUpdate();
        return new AdminCategoryResult(id, slug, command.label(), command.description(), command.sortOrder() == null ? 0 : command.sortOrder(), command.active() == null || command.active());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AdminCategoryResult> list() {
        List<Object[]> rows = em.createNativeQuery("""
            select id, slug, label, description, sort_order, is_active
            from category
            order by sort_order asc, label asc
        """).getResultList();
        return rows.stream().map(r -> new AdminCategoryResult(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                ((Number) r[4]).intValue(),
                (Boolean) r[5]
        )).toList();
    }

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

    @Override
    public void updateTaxonomy(UUID contentId, AdminUpdateContentTaxonomyCommand command) {
        em.createNativeQuery("delete from content_category where content_id = :contentId")
                .setParameter("contentId", contentId)
                .executeUpdate();
        em.createNativeQuery("delete from content_tag where content_id = :contentId")
                .setParameter("contentId", contentId)
                .executeUpdate();

        if (command.categorySlugs() != null) {
            for (String slug : command.categorySlugs()) {
                em.createNativeQuery("""
                    insert into content_category (content_id, category_id, created_at)
                    select :contentId, id, now() from category where slug = :slug
                    on conflict do nothing
                """)
                        .setParameter("contentId", contentId)
                        .setParameter("slug", slug)
                        .executeUpdate();
            }
        }
        if (command.tags() != null) {
            for (String raw : command.tags()) {
                String slug = normalizeSlug(raw);
                em.createNativeQuery("""
                    insert into tag (id, slug, label, created_at, updated_at)
                    values (:id, :slug, :label, now(), now())
                    on conflict (slug) do update set label = excluded.label, updated_at = now()
                """)
                        .setParameter("id", UUID.randomUUID())
                        .setParameter("slug", slug)
                        .setParameter("label", raw)
                        .executeUpdate();
                em.createNativeQuery("""
                    insert into content_tag (content_id, tag_id, created_at)
                    select :contentId, id, now() from tag where slug = :slug
                    on conflict do nothing
                """)
                        .setParameter("contentId", contentId)
                        .setParameter("slug", slug)
                        .executeUpdate();
            }
        }
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
