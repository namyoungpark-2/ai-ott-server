package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.TranscodingPort;
import com.aiott.ottpoc.application.port.out.TranscodingJobPort;
import com.aiott.ottpoc.application.port.out.ThumbnailPort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TranscodeVideoAssetService implements TranscodeVideoAssetUseCase {

    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodingJobPort jobPort;
    private final AssetStoragePort storagePort;
    private final TranscodingPort transcodingPort;
    private final ThumbnailPort thumbnailPort;

    @Async
    @Override
    public void transcode(UUID videoAssetId, UUID jobId) {
        if (jobId != null) jobPort.markRunning(jobId);

        var asset = videoAssetCommandPort.findById(videoAssetId)
                .orElseThrow(() -> new IllegalArgumentException("videoAsset not found: " + videoAssetId));

        try {
            videoAssetCommandPort.markTranscoding(videoAssetId);

            Path source = Path.of(asset.sourceKey()).toAbsolutePath();
            Path outputDir = storagePort.getHlsOutputDir(videoAssetId.toString()).toAbsolutePath();

            transcodingPort.transcodeToHls(source, outputDir);

            Path thumb = storagePort.getThumbnailPath(videoAssetId.toString());
            thumbnailPort.createJpegThumbnail(source, thumb, 2);

            Path master = outputDir.resolve("master.m3u8");
            videoAssetCommandPort.markReady(videoAssetId, master.toString());

            if (jobId != null) jobPort.markSucceeded(jobId);

        } catch (Exception e) {
            videoAssetCommandPort.markFailed(videoAssetId, e.getMessage());
            if (jobId != null) jobPort.markFailed(jobId, e.getMessage());
        }
    }
}

