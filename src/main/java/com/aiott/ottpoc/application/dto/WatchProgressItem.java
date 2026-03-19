package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record WatchProgressItem(
        UUID contentId,
        String title,
        String thumbnailUrl,
        long positionMs,
        Long durationMs,
        /** 0-100, null if durationMs is unknown */
        Integer progressPercent
) {}
