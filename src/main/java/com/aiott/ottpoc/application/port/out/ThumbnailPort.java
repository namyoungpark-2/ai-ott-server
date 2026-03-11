package com.aiott.ottpoc.application.port.out;

import java.nio.file.Path;

public interface ThumbnailPort {
    void createJpegThumbnail(Path inputVideo, Path outputJpeg, int seconds);
}
