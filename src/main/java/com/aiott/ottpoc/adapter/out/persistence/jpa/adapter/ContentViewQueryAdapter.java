package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.ContentViewResult;
import com.aiott.ottpoc.application.port.out.ContentViewQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentViewQueryAdapter implements ContentViewQueryPort {

    private final EntityManager em;

    @Override
    public ContentViewResult findById(UUID contentId, String lang) {

        String sql = """
          select
            c.id,
            coalesce(ci.title, 'Untitled') as title,
            case
              when va.status = 'READY' then 'READY'
              when va.status = 'FAILED' then 'FAILED'
              else 'PROCESSING'
            end as ui_status,
            case
              when va.status = 'READY'
                then concat('/hls/', va.id::text, '/master.m3u8')
              else null
            end as stream_url,
            null as thumbnail_url,
            va.error_message,
            case
              when va.id is not null then concat('/thumbnails/', va.id::text, '.jpg')
              else null
            end as thumbnail_url
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
          where c.id = :contentId
        """;

        Object[] r = (Object[]) em.createNativeQuery(sql)
                .setParameter("contentId", contentId)
                .setParameter("lang", lang)
                .getSingleResult();

        return new ContentViewResult(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4],
                (String) r[5]
        );
    }
}
