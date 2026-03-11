package com.aiott.ottpoc.application.port.out;

import java.nio.file.Path;

public interface AssetStoragePort {
    Path saveSourceVideo(byte[] bytes, String originalFilename);
    Path getHlsOutputDir(String baseName);
    Path getThumbnailPath(String baseName);
}
