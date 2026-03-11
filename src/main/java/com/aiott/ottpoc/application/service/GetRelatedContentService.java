package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.RelatedContentResult;
import com.aiott.ottpoc.application.port.in.GetRelatedContentUseCase;
import com.aiott.ottpoc.application.port.out.ContentQueryPort;
import com.aiott.ottpoc.domain.model.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRelatedContentService implements GetRelatedContentUseCase {

    private final ContentQueryPort contentQueryPort;

    @Override
    public List<RelatedContentResult> getRelated(UUID contentId, String lang, List<ContentType> types) {
        return contentQueryPort.loadRelated(contentId, lang, types);
    }
}
