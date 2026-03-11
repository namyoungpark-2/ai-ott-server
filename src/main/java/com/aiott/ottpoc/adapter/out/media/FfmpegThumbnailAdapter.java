package com.aiott.ottpoc.adapter.out.media;

import com.aiott.ottpoc.application.port.out.ThumbnailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public class FfmpegThumbnailAdapter implements ThumbnailPort {

    @Override
    public void createJpegThumbnail(Path inputVideo, Path outputJpeg, int seconds) {
        try {
            Files.createDirectories(outputJpeg.getParent());

            // -ss {seconds}: 특정 시점
            // -frames:v 1 : 1 프레임
            // -q:v 2 : 품질(낮을수록 고품질)
            List<String> cmd = List.of(
                    "ffmpeg",
                    "-y",
                    "-ss", String.valueOf(seconds),
                    "-i", inputVideo.toString(),
                    "-frames:v", "1",
                    "-q:v", "2",
                    outputJpeg.toString()
            );

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            log.info("[THUMB] ffmpeg start: {}", String.join(" ", cmd));
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.debug("[THUMB] {}", line);
                }
            }

            int code = p.waitFor();
            if (code != 0) {
                throw new RuntimeException("ffmpeg thumbnail failed with exitCode=" + code);
            }
            log.info("[THUMB] created: {}", outputJpeg);
        } catch (Exception e) {
            throw new RuntimeException("thumbnail generation failed", e);
        }
    }
}
