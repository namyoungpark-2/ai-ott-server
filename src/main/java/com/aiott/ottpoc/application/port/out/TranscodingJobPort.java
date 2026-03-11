package com.aiott.ottpoc.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface TranscodingJobPort {
    UUID createQueuedJob(UUID videoAssetId);
    void markRunning(UUID jobId);
    void markSucceeded(UUID jobId);
    void markFailed(UUID jobId, String errorMessage);

    boolean hasActiveJob(UUID videoAssetId);
    long countAttempts(UUID videoAssetId);
    Optional<JobInfo> findLatest(UUID videoAssetId);

    record JobInfo(String status, String errorMessage) {}
}
