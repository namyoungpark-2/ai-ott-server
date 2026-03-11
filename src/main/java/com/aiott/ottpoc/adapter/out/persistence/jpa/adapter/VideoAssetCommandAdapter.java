package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.VideoAssetJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.VideoAssetJpaRepository;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.domain.model.StorageType;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoAssetCommandAdapter implements VideoAssetCommandPort {

    private final VideoAssetJpaRepository repo;

    @Override
    public UUID createVideoAsset(UUID contentId, StorageType storage, String sourceKey, VideoAssetStatus status) {
        var e = new VideoAssetJpaEntity();
        e.setId(UUID.randomUUID());
        e.setContentId(contentId);
        e.setStorage(storage.name());
        e.setSourceKey(sourceKey);
        e.setStatus(status.name());
        e.setCreatedAt(OffsetDateTime.now());
        e.setUpdatedAt(OffsetDateTime.now());
        repo.save(e);
        return e.getId();
    }

    @Override
    public void markTranscoding(UUID videoAssetId) {
        var e = repo.findById(videoAssetId).orElseThrow();
        e.setStatus(VideoAssetStatus.TRANSCODING.name());
        e.setUpdatedAt(OffsetDateTime.now());
        repo.save(e);
    }

    @Override
    public void markReady(UUID videoAssetId, String hlsMasterKey) {
        var e = repo.findById(videoAssetId).orElseThrow();
        e.setStatus(VideoAssetStatus.READY.name());
        e.setHlsMasterKey(hlsMasterKey);

        e.setUpdatedAt(OffsetDateTime.now());
        repo.save(e);
    }

    @Override
    public void markFailed(UUID videoAssetId, String errorMessage) {
        var e = repo.findById(videoAssetId).orElseThrow();
        e.setStatus(VideoAssetStatus.FAILED.name());
        e.setErrorMessage(errorMessage);
        e.setUpdatedAt(OffsetDateTime.now());
        repo.save(e);
    }

    @Override
    public Optional<VideoAssetView> findReadyAssetByContentId(UUID contentId) {
        return repo.findFirstByContentIdAndStatus(contentId, VideoAssetStatus.READY.name())
                .map(e -> new VideoAssetView(e.getId(), e.getContentId(), e.getSourceKey(), e.getHlsMasterKey(), e.getStatus()));
    }

    @Override
    public Optional<VideoAssetView> findById(UUID videoAssetId) {
        return repo.findById(videoAssetId)
                .map(e -> new VideoAssetView(e.getId(), e.getContentId(), e.getSourceKey(), e.getHlsMasterKey(), e.getStatus()));
    }
}
