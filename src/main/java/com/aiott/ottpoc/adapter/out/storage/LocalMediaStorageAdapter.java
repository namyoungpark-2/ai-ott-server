package com.aiott.ottpoc.adapter.out.storage;

import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.domain.model.StorageType;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Local-disk implementation of {@link MediaStoragePort}.
 * Files are served by Spring's static resource handler (/hls/**, /thumbnails/**).
 */
public class LocalMediaStorageAdapter implements MediaStoragePort {

    @Override
    public StorageType storageType() {
        return StorageType.LOCAL;
    }

    @Override
    public String storeSource(Path localFile, UUID contentId, String filename) {
        // File is already on local disk (saved by LocalFileStorageAdapter).
        return localFile.toAbsolutePath().toString();
    }

    @Override
    public Path fetchSourceForTranscoding(String sourceKey, UUID assetId) {
        return Path.of(sourceKey);
    }

    @Override
    public String storeHls(UUID assetId, Path hlsDir) {
        // HLS dir was created inside ./data/hls/{assetId}/ by LocalFileStorageAdapter.
        // Just return the relative URL path served by WebConfig.
        return "/hls/" + assetId + "/master.m3u8";
    }

    @Override
    public String storeThumbnail(UUID assetId, Path thumbnailFile) {
        return "/thumbnails/" + assetId + ".jpg";
    }

    @Override
    public void cleanupTemp(Path... paths) {
        // Local mode: the local files ARE the permanent storage — do nothing.
    }
}
