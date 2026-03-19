package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.domain.model.StorageType;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Port for permanent media storage (Cloudflare R2, S3, or local).
 * <p>
 * Local mode:  files stay on disk, served via Spring static resource handlers.
 * R2 mode:     files are uploaded to Cloudflare R2; local temp files are deleted after upload.
 */
public interface MediaStoragePort {

    /** The storage type this adapter represents — persisted in video_asset.storage. */
    StorageType storageType();

    /**
     * Move the freshly-uploaded local temp file to permanent storage.
     * @return a storage key (local path or R2 object key) used later for transcoding.
     */
    String storeSource(Path localFile, UUID contentId, String filename);

    /**
     * Resolve the source key into a local {@link Path} ready for FFmpeg.
     * R2 adapter downloads the object to a temp file; local adapter just wraps the path.
     */
    Path fetchSourceForTranscoding(String sourceKey, UUID assetId);

    /**
     * Persist the transcoded HLS directory to permanent storage.
     * @return the public master playlist URL (absolute for R2, context-relative for local).
     */
    String storeHls(UUID assetId, Path hlsDir);

    /**
     * Persist the generated thumbnail to permanent storage.
     * @return the public thumbnail URL.
     */
    String storeThumbnail(UUID assetId, Path thumbnailFile);

    /**
     * Delete temporary local files/directories created during transcoding.
     * No-op in local mode (files ARE the permanent copy).
     */
    void cleanupTemp(Path... paths);

    /**
     * 재생 URL 반환.
     * <p>
     * Local: 저장된 경로/URL을 그대로 반환.<br>
     * R2: time-limited presigned URL 생성 (기본 4시간 유효).
     * <p>
     * 주의: HLS의 경우 master.m3u8 presigned URL만으로는 .ts 세그먼트까지
     * 완전히 보호되지 않습니다. Phase 2에서 Cloudflare Signed Token 또는
     * 서버 사이드 m3u8 재작성으로 업그레이드 필요.
     */
    default String getPlaybackUrl(String hlsMasterKey) {
        return hlsMasterKey;
    }
}
