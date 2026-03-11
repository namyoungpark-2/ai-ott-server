package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.ContentDailyWatchStatsResult;
import com.aiott.ottpoc.application.dto.TopContentWatchStatsResult;

import java.time.LocalDate;
import java.util.UUID;

public interface WatchAnalyticsUseCase {
    ContentDailyWatchStatsResult getContentDaily(UUID contentId, LocalDate from, LocalDate to);
    TopContentWatchStatsResult getTopContents(LocalDate from, LocalDate to, String metric, int limit);
}
