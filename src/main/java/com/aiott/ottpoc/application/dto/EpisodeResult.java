package com.aiott.ottpoc.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EpisodeResult(
        UUID id,
        int episodeNumber,
        String title,
        String description,
        Integer runtimeSeconds,
        OffsetDateTime releaseAt
) {}
