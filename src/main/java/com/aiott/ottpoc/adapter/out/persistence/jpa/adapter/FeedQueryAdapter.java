package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.FeedItemDto;
import com.aiott.ottpoc.application.port.out.FeedQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeedQueryAdapter implements FeedQueryPort {

    private final EntityManager em;

    @Override
    public List<FeedItemDto> loadFeed(String lang) {
        // MVP: title은 (요청 lang -> default_language -> 'en') 우선순위로 1개 선택
        String sql = """
          select
            c.id as content_id,
            coalesce(ci.title, 'Untitled') as title,
            case
              when va.status = 'READY' then 'READY'
              when va.status = 'FAILED' then 'FAILED'
              else 'PROCESSING'
            end as ui_status,
            case
              when va.id is not null then concat('/thumbnails/', va.id::text, '.jpg')
              else null
            end as thumbnail_url,
            c.content_type as content_type,
            case when c.content_type = 'EPISODE' then 'SERIES' else 'STANDALONE' end as kind,
            c.series_id as series_id,
            c.season_id as season_id,
            c.episode_number as episode_number
          from content c
          left join lateral (
            select ci1.title
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
          left join lateral (
            select va1.*
            from video_asset va1
            where va1.content_id = c.id
            order by va1.created_at desc
            limit 1
          ) va on true
          where c.status in ('DRAFT','PUBLISHED')
          order by c.created_at desc
          limit 200
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("lang", lang)
                .getResultList();

        return rows.stream().map(r -> new FeedItemDto(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4],
                (String) r[5],
                (UUID) r[6],
                (UUID) r[7],
                r[8] == null ? null : ((Number) r[8]).intValue()
        )).toList();
    }
}
