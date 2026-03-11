package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.ContentI18nJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.ContentJpaEntity;
import com.aiott.ottpoc.application.dto.EpisodeResult;
import com.aiott.ottpoc.application.port.out.SeasonQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeasonQueryAdapter implements SeasonQueryPort {

    private final EntityManager em;

    @Override
    public List<EpisodeResult> loadEpisodes(UUID seasonId, String lang) {
      String sql = """
        select
          c.id as id,
          c.episode_number as episodeNumber,
          coalesce(ci.title, '') as title,
          coalesce(ci.description, '') as description,
          c.runtime_seconds as runtimeSeconds,
          c.release_at as releaseAt
        from content c
        left join lateral (
          select ci1.title, ci1.description
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
        where c.season_id = :seasonId
          and c.content_type = 'EPISODE'
          and c.status = 'PUBLISHED'
        order by c.episode_number asc
      """;
    
      @SuppressWarnings("unchecked")
      List<Object[]> rows = em.createNativeQuery(sql)
          .setParameter("seasonId", seasonId)
          .setParameter("lang", lang)
          .getResultList();
    
      return rows.stream().map(r -> new EpisodeResult(
          (UUID) r[0],
          ((Number) r[1]).intValue(),
          (String) r[2],
          (String) r[3],
          r[4] == null ? null : ((Number) r[4]).intValue(),
          (java.time.OffsetDateTime) r[5]
      )).toList();
    }
}
