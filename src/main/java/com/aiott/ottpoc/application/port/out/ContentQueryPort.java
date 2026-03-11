package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.RelatedContentResult;
import com.aiott.ottpoc.domain.model.ContentType;

import java.util.List;
import java.util.UUID;

public interface ContentQueryPort {
    List<RelatedContentResult> loadRelated(UUID sourceContentId, String lang, List<ContentType> types);
}
