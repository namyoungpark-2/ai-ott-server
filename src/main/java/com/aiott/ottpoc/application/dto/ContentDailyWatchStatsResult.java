package com.aiott.ottpoc.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContentDailyWatchStatsResult(
        UUID contentId,
        LocalDate from,
        LocalDate to,
        List<DayStat> series
) {
    public record DayStat(
            LocalDate date,
            int playCount,
            int uniqueDevices,
            long watchTimeMs,
            int completeCount
    ) {}
}
