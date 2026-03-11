package com.aiott.ottpoc.application.port.in;

import java.util.UUID;

public interface RetryTranscodingUseCase {
    RetryResult retry(UUID videoAssetId);

    record RetryResult(
            boolean accepted,
            String reason,        // accepted=false면 이유
            UUID jobId,
            long attemptCount
    ) {}
}
