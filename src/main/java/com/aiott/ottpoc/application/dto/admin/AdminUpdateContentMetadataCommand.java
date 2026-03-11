package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;

public record AdminUpdateContentMetadataCommand(
        String lang,
        String title,
        String description,
        Integer runtimeSeconds,
        OffsetDateTime releaseAt,
        String posterUrl,
        String bannerUrl,
        String ageRating,
        Boolean featured,
        String status
) {}
