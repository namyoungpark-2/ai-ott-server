package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.port.out.ContentCommandPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentCommandAdapter implements ContentCommandPort {

    private final EntityManager em;

    @Override
    @Transactional
    public UUID createMovieContent(String title) {
        UUID id = UUID.randomUUID();

        // content_i18n도 같이 넣어줘야 조회가 편해짐(요청 lang이 en이라고 가정)
        em.createNativeQuery("""
          insert into content
            (id, content_type, status, default_language, created_at, updated_at)
          values
            (:id, 'MOVIE', 'DRAFT', 'en', now(), now())
        """)
        .setParameter("id", id)
        .executeUpdate();

        em.createNativeQuery("""
          insert into content_i18n
            (content_id, lang, title, description, created_at, updated_at)
          values
            (:id, 'en', :title, null, now(), now())
          on conflict (content_id, lang) do nothing
        """)
        .setParameter("id", id)
        .setParameter("title", title)
        .executeUpdate();

        return id;
    }

    @Override
    @Transactional
    public void publish(UUID contentId) {
        em.createNativeQuery("""
          update content
          set status = 'PUBLISHED', updated_at = now()
          where id = :id
        """)
        .setParameter("id", contentId)
        .executeUpdate();
    }
}
