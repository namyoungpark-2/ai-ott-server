package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.SeriesI18nJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.SeriesJpaEntity;
import com.aiott.ottpoc.application.dto.SeriesDetailResult;
import com.aiott.ottpoc.application.dto.SeasonResult;
import com.aiott.ottpoc.application.port.out.SeriesQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeriesQueryAdapter implements SeriesQueryPort {

    private final EntityManager em;

    @Override
    public SeriesDetailResult.SeriesMeta loadSeriesMeta(UUID seriesId, String lang) {

        return em.createQuery("""
            select new com.aiott.ottpoc.application.dto.SeriesDetailResult.SeriesMeta(
                s.id,
                si.title,
                si.description,
                s.status,
                s.defaultLanguage
            )
            from SeriesJpaEntity s
            join SeriesI18nJpaEntity si
              on si.seriesId = s.id and si.lang = :lang
            where s.id = :seriesId
        """, SeriesDetailResult.SeriesMeta.class)
        .setParameter("seriesId", seriesId)
        .setParameter("lang", lang)
        .getSingleResult();
    }

    @Override
    public List<SeasonResult> loadSeasons(UUID seriesId, String lang) {

        return em.createQuery("""
            select new com.aiott.ottpoc.application.dto.SeasonResult.SeasonResult(
                se.id,
                se.seasonNumber,
                si.title,
                si.description
            )
            from SeasonJpaEntity se
            left join SeasonI18nJpaEntity si
              on si.seasonId = se.id and si.lang = :lang
            where se.series.id = :seriesId
            order by se.seasonNumber asc
        """, SeasonResult.class)
        .setParameter("seriesId", seriesId)
        .setParameter("lang", lang)
        .getResultList();
    }
}
