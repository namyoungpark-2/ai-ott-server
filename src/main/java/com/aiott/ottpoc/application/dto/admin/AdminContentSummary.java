package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminContentSummary(
        UUID contentId,
        String title,

        String contentStatus,      // DRAFT|PUBLISHED|...
        String uiStatus,           // PROCESSING|READY|FAILED

        UUID videoAssetId,
        String videoAssetStatus,   // UPLOADED|TRANSCODING|READY|FAILED

        long attemptCount,
        String latestJobStatus,    // QUEUED|RUNNING|SUCCEEDED|FAILED
        String latestErrorMessage,

        String thumbnailUrl,
        String streamUrl,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,

        String channelHandle,
        String channelName
) {}
