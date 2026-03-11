package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.RelatedContentResult;
import com.aiott.ottpoc.domain.model.ContentType;

import java.util.List;
import java.util.UUID;

public interface GetRelatedContentUseCase {
    List<RelatedContentResult> getRelated(UUID contentId, String lang, List<ContentType> types);
}
