package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

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
        Object[] r = (Object[]) em.createNativeQuery("""
            select s.id, coalesce(si.title, 'Untitled') as title, si.description,
                   s.status, s.default_language
            from series s
            left join lateral (
                select si1.title, si1.description
                from series_i18n si1
                where si1.series_id = s.id
                  and si1.lang in (:lang, s.default_language, 'en')
                order by case when si1.lang = :lang then 0
                              when si1.lang = s.default_language then 1
                              else 2 end
                limit 1
            ) si on true
            where s.id = :seriesId
        """)
                .setParameter("seriesId", seriesId)
                .setParameter("lang", lang)
                .getSingleResult();

        return new SeriesDetailResult.SeriesMeta(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4]
        );
    }

    @Override
    public List<SeasonResult> loadSeasons(UUID seriesId, String lang) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            select se.id, se.season_number,
                   coalesce(si.title, concat('Season ', se.season_number)) as title,
                   si.description
            from season se
            left join lateral (
                select si1.title, si1.description
                from season_i18n si1
                where si1.season_id = se.id
                  and si1.lang in (:lang, 'en')
                order by case when si1.lang = :lang then 0 else 1 end
                limit 1
            ) si on true
            where se.series_id = :seriesId
            order by se.season_number asc
        """)
                .setParameter("seriesId", seriesId)
                .setParameter("lang", lang)
                .getResultList();

        return rows.stream().map(r -> new SeasonResult(
                (UUID) r[0],
                ((Number) r[1]).intValue(),
                (String) r[2],
                (String) r[3]
        )).toList();
    }
}
