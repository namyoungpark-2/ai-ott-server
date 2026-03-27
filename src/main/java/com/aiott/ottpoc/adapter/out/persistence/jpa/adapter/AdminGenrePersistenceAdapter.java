package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.admin.AdminCreateGenreCommand;
import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;
import com.aiott.ottpoc.application.port.out.AdminGenreCommandPort;
import com.aiott.ottpoc.application.port.out.AdminGenreQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminGenrePersistenceAdapter implements AdminGenreCommandPort, AdminGenreQueryPort {
    private final EntityManager em;

    @Override
    public AdminGenreResult create(AdminCreateGenreCommand command) {
        UUID id = UUID.randomUUID();
        String slug = normalizeSlug(command.slug() == null || command.slug().isBlank() ? command.label() : command.slug());
        String lang = command.lang() == null || command.lang().isBlank() ? "en" : command.lang();
        int sortOrder = command.sortOrder() == null ? 0 : command.sortOrder();
        boolean active = command.active() == null || command.active();

        em.createNativeQuery("""
            insert into genre (id, slug, label, description, sort_order, is_active, default_language, created_at, updated_at)
            values (:id, :slug, :label, :description, :sortOrder, :active, :lang, now(), now())
        """)
                .setParameter("id", id).setParameter("slug", slug)
                .setParameter("label", command.label()).setParameter("description", command.description())
                .setParameter("sortOrder", sortOrder).setParameter("active", active).setParameter("lang", lang)
                .executeUpdate();

        em.createNativeQuery("""
            insert into genre_i18n (genre_id, lang, label, description, created_at, updated_at)
            values (:genreId, :lang, :label, :description, now(), now())
            on conflict (genre_id, lang) do update
                set label = excluded.label, description = excluded.description, updated_at = now()
        """)
                .setParameter("genreId", id).setParameter("lang", lang)
                .setParameter("label", command.label()).setParameter("description", command.description())
                .executeUpdate();

        return new AdminGenreResult(id, slug, command.label(), command.description(), sortOrder, active);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AdminGenreResult> list() {
        List<Object[]> rows = em.createNativeQuery("""
            select g.id, g.slug, coalesce(gi.label, g.label), coalesce(gi.description, g.description),
                   g.sort_order, g.is_active
            from genre g
            left join genre_i18n gi on gi.genre_id = g.id and gi.lang = g.default_language
            order by g.sort_order asc, coalesce(gi.label, g.label) asc
        """).getResultList();
        return rows.stream().map(r -> new AdminGenreResult(
                (UUID) r[0], (String) r[1], (String) r[2], (String) r[3],
                ((Number) r[4]).intValue(), (Boolean) r[5]
        )).toList();
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
