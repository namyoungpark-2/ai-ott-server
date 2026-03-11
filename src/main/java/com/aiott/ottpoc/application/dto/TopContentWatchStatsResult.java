package com.aiott.ottpoc.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TopContentWatchStatsResult(
        LocalDate from,
        LocalDate to,
        String metric,
        int limit,
        List<Item> items
) {
    public record Item(
            UUID contentId,
            long watchTimeMs,
            long playCount,
            long completeCount,
            long uniqueDevices
    ) {}
}
