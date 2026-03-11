package com.aiott.ottpoc.application.port.out;

import java.nio.file.Path;

public interface TranscodingPort {
    void transcodeToHls(Path sourceMp4, Path outputDir);
    void createThumbnail(Path sourceMp4, Path thumbnailPath);
}
