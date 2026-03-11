package com.aiott.ottpoc.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RelatedContentResult(
        UUID id,
        String contentType,
        String title,
        String description,
        Integer runtimeSeconds,
        OffsetDateTime releaseAt,
        String sourceRelation
) {}
