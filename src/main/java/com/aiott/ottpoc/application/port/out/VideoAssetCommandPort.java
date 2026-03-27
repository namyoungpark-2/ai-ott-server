package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.domain.model.StorageType;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;

import java.util.Optional;
import java.util.UUID;

public interface VideoAssetCommandPort {

    UUID createVideoAsset(UUID contentId, StorageType storage, String sourceKey, VideoAssetStatus status);

    void markTranscoding(UUID videoAssetId);

    void markReady(UUID videoAssetId, String hlsMasterKey);

    void markFailed(UUID videoAssetId, String errorMessage);

    void updateMediaMetadata(UUID videoAssetId, Integer videoWidth, Integer videoHeight, Long durationMs);

    Optional<VideoAssetView> findReadyAssetByContentId(UUID contentId);

    Optional<VideoAssetView> findById(UUID videoAssetId);

    Optional<VideoAssetView> findLatestByContentId(UUID contentId);

    record VideoAssetView(
            UUID id,
            UUID contentId,
            String sourceKey,
            String hlsMasterKey,
            String status,
            Integer videoWidth,
            Integer videoHeight,
            Long durationMs
    ) {}
}
