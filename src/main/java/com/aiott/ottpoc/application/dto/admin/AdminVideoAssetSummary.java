package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminVideoAssetSummary(
        UUID videoAssetId,
        UUID contentId,
        String status,

        long attemptCount,
        String latestJobStatus,
        String latestErrorMessage,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
