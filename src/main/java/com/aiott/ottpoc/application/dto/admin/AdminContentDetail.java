package com.aiott.ottpoc.application.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminContentDetail(
        UUID contentId,
        String title,

        String contentStatus,
        String uiStatus,

        UUID videoAssetId,
        String videoAssetStatus,

        String sourceKey,          // video_asset.source_key (디버깅용)
        String hlsMasterKey,       // video_asset.hls_master_key (디버깅용)
        String videoAssetErrorMessage,

        long attemptCount,
        String latestJobStatus,
        String latestErrorMessage,

        String thumbnailUrl,
        String streamUrl,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
