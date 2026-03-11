package com.aiott.ottpoc.application.port.out;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OpsTranscodingQueryPort {

    record SummaryRow(
            long totalJobs,
            long successCount,
            long failedCount,
            long runningCount,
            Double avgProcessingSeconds
    ) {}

    record FailureTopRow(
            String errorMessage,
            long count
    ) {}

    record RecentRow(
            UUID jobId,
            UUID videoAssetId,
            String status,
            String errorMessage,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    SummaryRow fetchSummary();
    List<FailureTopRow> fetchFailureTop(int limit);
    List<RecentRow> fetchRecent(int limit);
}
