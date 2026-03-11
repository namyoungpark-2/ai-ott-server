package com.aiott.ottpoc.adapter.out.storage.r2;

import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.config.R2Properties;
import com.aiott.ottpoc.domain.model.StorageType;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

/**
 * Cloudflare R2 implementation of {@link MediaStoragePort}.
 * Uses the S3-compatible API provided by R2.
 * <p>
 * Source videos and HLS segments are stored in R2 and served via the R2 public URL.
 * Local temp files are cleaned up after each operation.
 */
@Slf4j
public class R2MediaStorageAdapter implements MediaStoragePort {

    private final S3Client s3;
    private final String bucket;
    private final String publicUrl;
    private final Path tempDir;

    public R2MediaStorageAdapter(S3Client s3, R2Properties props, String tempDir) {
        this.s3 = s3;
        this.bucket = props.getBucket();
        this.publicUrl = props.getPublicUrl().replaceAll("/$", "");
        this.tempDir = Paths.get(tempDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.tempDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp dir: " + this.tempDir, e);
        }
    }

    @Override
    public StorageType storageType() {
        return StorageType.R2;
    }

    @Override
    public String storeSource(Path localFile, UUID contentId, String filename) {
        String safe = sanitize(filename);
        String key = "source/" + contentId + "/" + System.currentTimeMillis() + "_" + safe;
        log.info("[R2] Uploading source {} → {}", localFile, key);
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(detectVideoContentType(filename))
                        .build(),
                RequestBody.fromFile(localFile));
        silentDelete(localFile);
        log.info("[R2] Source uploaded: {}", key);
        return key;
    }

    @Override
    public Path fetchSourceForTranscoding(String sourceKey, UUID assetId) {
        Path target = tempDir.resolve("src_" + assetId + ".mp4");
        log.info("[R2] Downloading source {} → {}", sourceKey, target);
        s3.getObject(
                GetObjectRequest.builder().bucket(bucket).key(sourceKey).build(),
                target);
        return target;
    }

    @Override
    public String storeHls(UUID assetId, Path hlsDir) {
        String prefix = "hls/" + assetId + "/";
        log.info("[R2] Uploading HLS directory {} → {}", hlsDir, prefix);
        try (var stream = Files.walk(hlsDir)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                String relPath = hlsDir.relativize(file).toString().replace('\\', '/');
                String key = prefix + relPath;
                String contentType = key.endsWith(".m3u8")
                        ? "application/vnd.apple.mpegurl"
                        : "video/mp2t";
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        RequestBody.fromFile(file));
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload HLS for assetId=" + assetId, e);
        }
        String masterUrl = publicUrl + "/" + prefix + "master.m3u8";
        log.info("[R2] HLS uploaded, master URL: {}", masterUrl);
        return masterUrl;
    }

    @Override
    public String storeThumbnail(UUID assetId, Path thumbnailFile) {
        if (!Files.exists(thumbnailFile)) {
            log.warn("[R2] Thumbnail file not found, skipping: {}", thumbnailFile);
            return null;
        }
        String key = "thumbnails/" + assetId + ".jpg";
        log.info("[R2] Uploading thumbnail {} → {}", thumbnailFile, key);
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("image/jpeg")
                        .build(),
                RequestBody.fromFile(thumbnailFile));
        return publicUrl + "/" + key;
    }

    @Override
    public void cleanupTemp(Path... paths) {
        for (Path path : paths) {
            if (path == null) continue;
            try {
                if (Files.isDirectory(path)) {
                    Files.walkFileTree(path, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.deleteIfExists(file);
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.deleteIfExists(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e) {
                log.warn("[R2] Failed to delete temp path: {}", path, e);
            }
        }
    }

    private static void silentDelete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[R2] Failed to delete temp file: {}", path, e);
        }
    }

    private static String sanitize(String filename) {
        if (filename == null) return "upload.mp4";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String detectVideoContentType(String filename) {
        if (filename == null) return "video/mp4";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        return "video/mp4";
    }
}
