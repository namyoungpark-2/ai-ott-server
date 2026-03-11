package com.aiott.ottpoc.application.port.in;

import java.util.UUID;

public interface TranscodeVideoAssetUseCase {
    void transcode(UUID videoAssetId, UUID jobId);
}
