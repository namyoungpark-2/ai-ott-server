package com.aiott.ottpoc.adapter.out.storage;

import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
public class LocalFileStorageAdapter implements AssetStoragePort {

    private final Path uploadDir;
    private final Path hlsDir;

    public LocalFileStorageAdapter(
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.hls-dir}") String hlsDir
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.hlsDir = Paths.get(hlsDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadDir);
            Files.createDirectories(this.hlsDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path saveSourceVideo(byte[] bytes, String originalFilename) {
        String safeName = (originalFilename == null ? "upload.mp4" : originalFilename).replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = uploadDir.resolve(System.currentTimeMillis() + "_" + safeName);
        try {
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return target;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + target, e);
        }
    }

    @Override
    public Path getHlsOutputDir(String baseName) {
        Path dir = hlsDir.resolve(baseName);
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create dir: " + dir, e);
        }
    }

    @Override
    public Path getThumbnailPath(String baseName) {
        return Path.of("./data/thumbnails").resolve(baseName + ".jpg");
    }

}
