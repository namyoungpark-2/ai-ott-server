package com.aiott.ottpoc.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WatchAnalyticsQueryPort {

    record DailyRow(LocalDate date, int playCount, int uniqueDevices, long watchTimeMs, int completeCount) {}

    List<DailyRow> findDaily(UUID contentId, LocalDate from, LocalDate to);

    record TopRow(UUID contentId, long watchTimeMs, long playCount, long completeCount, long uniqueDevices) {}
    List<TopRow> findTop(LocalDate from, LocalDate to, int limit, String metric);
}
