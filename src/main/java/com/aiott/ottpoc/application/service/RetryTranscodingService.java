package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.RetryTranscodingUseCase;
import com.aiott.ottpoc.application.port.out.TranscodingJobPort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetryTranscodingService implements RetryTranscodingUseCase {

    private final TranscodingJobPort jobPort;
    private final VideoAssetCommandPort videoAssetPort;
    private final TranscodeVideoAssetService transcodeService;

    @Override
    @Transactional
    public RetryResult retry(UUID videoAssetId) {
        // videoAsset 존재 여부 확인
        if (videoAssetPort.findById(videoAssetId).isEmpty()) {
            return new RetryResult(false, "VIDEO_ASSET_NOT_FOUND", null, 0);
        }

        // 이미 진행 중이면 중복 큐 방지
        if (jobPort.hasActiveJob(videoAssetId)) {
            return new RetryResult(false, "ALREADY_IN_PROGRESS", null, jobPort.countAttempts(videoAssetId));
        }

        // 새 attempt 생성
        UUID jobId = jobPort.createQueuedJob(videoAssetId);
        long attempts = jobPort.countAttempts(videoAssetId);

        // Async 트랜스코딩 시작 (jobId를 전달할 수 있으면 전달하는 게 베스트)
        transcodeService.transcode(videoAssetId, jobId);

        return new RetryResult(true, null, jobId, attempts);
    }
}
