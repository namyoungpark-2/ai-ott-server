package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.ContentDailyWatchStatsResult;
import com.aiott.ottpoc.application.dto.TopContentWatchStatsResult;
import com.aiott.ottpoc.application.port.in.WatchAnalyticsUseCase;
import com.aiott.ottpoc.application.port.out.WatchAnalyticsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchAnalyticsService implements WatchAnalyticsUseCase {

    private final WatchAnalyticsQueryPort queryPort;

    @Override
    public ContentDailyWatchStatsResult getContentDaily(UUID contentId, LocalDate from, LocalDate to) {
        var rows = queryPort.findDaily(contentId, from, to);
        var series = rows.stream()
                .map(r -> new ContentDailyWatchStatsResult.DayStat(r.date(), r.playCount(), r.uniqueDevices(), r.watchTimeMs(), r.completeCount()))
                .toList();
        return new ContentDailyWatchStatsResult(contentId, from, to, series);
    }

    @Override
    public TopContentWatchStatsResult getTopContents(LocalDate from, LocalDate to, String metric, int limit) {
        var rows = queryPort.findTop(from, to, limit, metric);
        var items = rows.stream()
                .map(r -> new TopContentWatchStatsResult.Item(r.contentId(), r.watchTimeMs(), r.playCount(), r.completeCount(), r.uniqueDevices()))
                .toList();
        return new TopContentWatchStatsResult(from, to, metric, limit, items);
    }
}
