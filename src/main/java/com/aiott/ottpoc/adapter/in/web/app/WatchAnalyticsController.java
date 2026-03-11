package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.ContentDailyWatchStatsResult;
import com.aiott.ottpoc.application.dto.TopContentWatchStatsResult;
import com.aiott.ottpoc.application.port.in.WatchAnalyticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/analytics")
public class WatchAnalyticsController {

    private final WatchAnalyticsUseCase analyticsUseCase;

    @GetMapping("/contents/{contentId}/daily")
    public ContentDailyWatchStatsResult daily(
            @PathVariable UUID contentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return analyticsUseCase.getContentDaily(contentId, from, to);
    }

    @GetMapping("/contents/top")
    public TopContentWatchStatsResult top(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "watchTimeMs") String metric,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return analyticsUseCase.getTopContents(from, to, metric, limit);
    }
}
