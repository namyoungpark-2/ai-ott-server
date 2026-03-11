package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.application.dto.RelatedContentResult;
import com.aiott.ottpoc.application.port.out.ContentQueryPort;
import com.aiott.ottpoc.domain.model.ContentType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentQueryAdapter implements ContentQueryPort {

    private final EntityManager em;

    @Override
    public List<RelatedContentResult> loadRelated(UUID sourceContentId, String lang, List<ContentType> types) {
        var typeStrings = types.stream().map(Enum::name).toList();

        return em.createQuery("""
            select new com.aiottpoc.application.dto.RelatedContentResult(
              c.id,
              c.contentType,
              ci.title,
              ci.description,
              c.runtimeSeconds,
              c.releaseAt,
              c.sourceRelation
            )
            from ContentJpaEntity c
            left join ContentI18nJpaEntity ci
              on ci.contentId = c.id and ci.lang = :lang
            where c.sourceContent.id = :sourceContentId
              and c.status = 'PUBLISHED'
              and c.contentType in :types
            order by c.releaseAt desc nulls last, c.id desc
        """, RelatedContentResult.class)
        .setParameter("sourceContentId", sourceContentId)
        .setParameter("lang", lang)
        .setParameter("types", typeStrings)
        .getResultList();
    }
}
