package com.aiott.ottpoc.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CatalogBrowseItemResult(
        UUID id,
        String title,
        String description,
        String contentType,
        String status,
        String posterUrl,
        String bannerUrl,
        Integer runtimeSeconds,
        OffsetDateTime releaseAt,
        UUID seriesId,
        UUID seasonId,
        Integer episodeNumber,
        List<String> categories,
        List<String> tags
) {}
