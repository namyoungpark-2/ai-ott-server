package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.application.port.out.TranscodingPort;
import com.aiott.ottpoc.application.port.out.TranscodingJobPort;
import com.aiott.ottpoc.application.port.out.ThumbnailPort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodeVideoAssetService implements TranscodeVideoAssetUseCase {

    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodingJobPort jobPort;
    private final AssetStoragePort storagePort;
    private final MediaStoragePort mediaStoragePort;
    private final TranscodingPort transcodingPort;
    private final ThumbnailPort thumbnailPort;

    @Async
    @Override
    public void transcode(UUID videoAssetId, UUID jobId) {
        if (jobId != null) jobPort.markRunning(jobId);

        var asset = videoAssetCommandPort.findById(videoAssetId)
                .orElseThrow(() -> new IllegalArgumentException("videoAsset not found: " + videoAssetId));

        Path source = null;
        Path outputDir = null;
        Path thumb = null;

        try {
            videoAssetCommandPort.markTranscoding(videoAssetId);

            // Resolve source to a local path (downloads from R2 if needed)
            source = mediaStoragePort.fetchSourceForTranscoding(asset.sourceKey(), videoAssetId);

            // Create local temp dirs for FFmpeg output
            outputDir = storagePort.getHlsOutputDir(videoAssetId.toString()).toAbsolutePath();
            thumb = storagePort.getThumbnailPath(videoAssetId.toString());

            // Transcode and generate thumbnail locally
            transcodingPort.transcodeToHls(source, outputDir);
            thumbnailPort.createJpegThumbnail(source, thumb, 2);

            // Persist to permanent storage (local or R2) and get the public master URL
            String hlsMasterKey = mediaStoragePort.storeHls(videoAssetId, outputDir);
            mediaStoragePort.storeThumbnail(videoAssetId, thumb);

            videoAssetCommandPort.markReady(videoAssetId, hlsMasterKey);
            if (jobId != null) jobPort.markSucceeded(jobId);

            log.info("[TRANSCODE] Done assetId={} masterUrl={}", videoAssetId, hlsMasterKey);

        } catch (Exception e) {
            log.error("[TRANSCODE] Failed assetId={}", videoAssetId, e);
            videoAssetCommandPort.markFailed(videoAssetId, e.getMessage());
            if (jobId != null) jobPort.markFailed(jobId, e.getMessage());
        } finally {
            // In R2 mode, delete local temp files. In local mode, this is a no-op.
            mediaStoragePort.cleanupTemp(source, outputDir, thumb);
        }
    }
}
