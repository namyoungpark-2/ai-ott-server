package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.TranscodingJobJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.TranscodingJobJpaRepository;
import com.aiott.ottpoc.application.port.out.TranscodingJobPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TranscodingJobPersistenceAdapter implements TranscodingJobPort {

    private final TranscodingJobJpaRepository repo;

    @Override
    @Transactional
    public UUID createQueuedJob(UUID videoAssetId) {
        var e = new TranscodingJobJpaEntity();
        e.setId(UUID.randomUUID());
        e.setVideoAssetId(videoAssetId);
        e.setStatus("QUEUED");
        e.setCreatedAt(OffsetDateTime.now());
        e.setUpdatedAt(OffsetDateTime.now());
        repo.save(e);
        return e.getId();
    }

    @Override @Transactional
    public void markRunning(UUID jobId) {
        repo.findById(jobId).ifPresent(e -> {
            e.setStatus("RUNNING");
            e.setUpdatedAt(OffsetDateTime.now());
        });
    }

    @Override @Transactional
    public void markSucceeded(UUID jobId) {
        repo.findById(jobId).ifPresent(e -> {
            e.setStatus("SUCCEEDED");
            e.setErrorMessage(null);
            e.setUpdatedAt(OffsetDateTime.now());
        });
    }

    @Override @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        repo.findById(jobId).ifPresent(e -> {
            e.setStatus("FAILED");
            e.setErrorMessage(errorMessage);
            e.setUpdatedAt(OffsetDateTime.now());
        });
    }

    @Override
    public boolean hasActiveJob(UUID videoAssetId) {
        return repo.existsActiveJob(videoAssetId);
    }

    @Override
    public long countAttempts(UUID videoAssetId) {
        return repo.countByVideoAssetId(videoAssetId);
    }

    @Override
    public Optional<JobInfo> findLatest(UUID videoAssetId) {
        return repo.findLatest(videoAssetId).map(e -> new JobInfo(e.getStatus(), e.getErrorMessage()));
    }
}
