package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogCommandAdapter implements CatalogCommandPort {

    private final EntityManager em;

    @Override
    @Transactional
    public UUID createMovieContent(String title) {
        UUID id = UUID.randomUUID();

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
    public UUID createSeries(String title, String defaultLang) {
        UUID id = UUID.randomUUID();

        em.createNativeQuery("""
          insert into series
            (id, status, default_language, created_at, updated_at)
          values
            (:id, 'DRAFT', :defaultLang, now(), now())
        """)
        .setParameter("id", id)
        .setParameter("defaultLang", defaultLang)
        .executeUpdate();

        em.createNativeQuery("""
          insert into series_i18n
            (series_id, lang, title, description, created_at, updated_at)
          values
            (:id, :lang, :title, null, now(), now())
          on conflict (series_id, lang) do nothing
        """)
        .setParameter("id", id)
        .setParameter("lang", defaultLang)
        .setParameter("title", title)
        .executeUpdate();

        return id;
    }

    @Override
    @Transactional
    public UUID ensureSeason(UUID seriesId, int seasonNumber, String defaultLang) {
        // 기존 시즌이 있는지 확인
        var existingSeasonId = (UUID) em.createNativeQuery("""
          select id from season
          where series_id = :seriesId and season_number = :seasonNumber
        """)
        .setParameter("seriesId", seriesId)
        .setParameter("seasonNumber", seasonNumber)
        .getResultStream()
        .findFirst()
        .orElse(null);

        if (existingSeasonId != null) {
            return existingSeasonId;
        }

        // 새 시즌 생성
        UUID newSeasonId = UUID.randomUUID();

        em.createNativeQuery("""
          insert into season
            (id, series_id, season_number, status, created_at, updated_at)
          values
            (:id, :seriesId, :seasonNumber, 'DRAFT', now(), now())
        """)
        .setParameter("id", newSeasonId)
        .setParameter("seriesId", seriesId)
        .setParameter("seasonNumber", seasonNumber)
        .executeUpdate();

        // 시즌 제목은 기본적으로 "Season {number}" 형식
        String seasonTitle = "Season " + seasonNumber;
        em.createNativeQuery("""
          insert into season_i18n
            (season_id, lang, title, description, created_at, updated_at)
          values
            (:seasonId, :lang, :title, null, now(), now())
          on conflict (season_id, lang) do nothing
        """)
        .setParameter("seasonId", newSeasonId)
        .setParameter("lang", defaultLang)
        .setParameter("title", seasonTitle)
        .executeUpdate();

        return newSeasonId;
    }

    @Override
    @Transactional
    public UUID createEpisodeContent(UUID seriesId, UUID seasonId, int episodeNumber, String title) {
        UUID contentId = UUID.randomUUID();

        em.createNativeQuery("""
          insert into content
            (id, content_type, status, series_id, season_id, episode_number, default_language, created_at, updated_at)
          values
            (:id, 'EPISODE', 'DRAFT', :seriesId, :seasonId, :episodeNumber, 'en', now(), now())
        """)
        .setParameter("id", contentId)
        .setParameter("seriesId", seriesId)
        .setParameter("seasonId", seasonId)
        .setParameter("episodeNumber", episodeNumber)
        .executeUpdate();

        em.createNativeQuery("""
          insert into content_i18n
            (content_id, lang, title, description, created_at, updated_at)
          values
            (:id, 'en', :title, null, now(), now())
          on conflict (content_id, lang) do nothing
        """)
        .setParameter("id", contentId)
        .setParameter("title", title)
        .executeUpdate();

        return contentId;
    }

    @Override
    public int nextEpisodeNumber(UUID seasonId) {
        var result = em.createNativeQuery("""
          select coalesce(max(episode_number), 0) + 1
          from content
          where season_id = :seasonId
            and content_type = 'EPISODE'
        """)
        .setParameter("seasonId", seasonId)
        .getSingleResult();

        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        return 1; // 기본값
    }


    @Override
    @Transactional
    public void updateContentStatus(UUID id, String status) {
        em.createNativeQuery("""
          update content
          set status = :status
          where id = :id
        """)
        .setParameter("id", id)
        .setParameter("status", status)
        .executeUpdate();
    }
}
