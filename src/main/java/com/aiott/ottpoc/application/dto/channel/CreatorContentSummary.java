package com.aiott.ottpoc.application.dto.channel;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreatorContentSummary(
        UUID contentId,
        String title,
        String contentType,
        String status,
        String videoAssetStatus,
        String thumbnailUrl,
        OffsetDateTime createdAt
) {}
