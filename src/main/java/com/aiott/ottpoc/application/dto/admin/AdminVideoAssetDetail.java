package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminVideoAssetDetail(
        UUID videoAssetId,
        UUID contentId,
        String status,

        String sourceUrl,
        String hlsUrl,
        String thumbnailUrl,

        long attemptCount,
        String latestJobStatus,
        String latestErrorMessage,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
