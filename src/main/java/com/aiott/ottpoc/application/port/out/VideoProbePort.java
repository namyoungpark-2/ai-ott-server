package com.aiott.ottpoc.application.port.out;

import java.nio.file.Path;

public interface VideoProbePort {

    VideoMetadata probe(Path videoFile);

    record VideoMetadata(int width, int height, long durationMs) {
        public String orientation() {
            if (width == 0 || height == 0) return "LANDSCAPE";
            double ratio = (double) width / height;
            if (ratio >= 1.2) return "LANDSCAPE";
            if (ratio <= 0.8) return "PORTRAIT";
            return "SQUARE";
        }
    }
}
