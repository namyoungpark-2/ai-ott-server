package com.aiott.ottpoc.adapter.out.media;

import com.aiott.ottpoc.application.port.out.VideoProbePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FfprobeAdapter implements VideoProbePort {

    private final String ffprobePath;

    public FfprobeAdapter(@Value("${app.ffmpeg.path:ffmpeg}") String ffmpegPath) {
        // ffprobe is in the same directory as ffmpeg
        this.ffprobePath = ffmpegPath.replace("ffmpeg", "ffprobe");
    }

    @Override
    public VideoMetadata probe(Path videoFile) {
        try {
            List<String> cmd = List.of(
                    ffprobePath,
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,duration",
                    "-show_entries", "format=duration",
                    "-of", "flat",
                    videoFile.toString()
            );

            var process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            String output = new String(process.getInputStream().readAllBytes());
            int code = process.waitFor();

            if (code != 0) {
                log.warn("[PROBE] ffprobe failed code={} output={}", code, output);
                return new VideoMetadata(0, 0, 0);
            }

            int width = extractInt(output, "streams\\.stream\\.0\\.width=(\\d+)");
            int height = extractInt(output, "streams\\.stream\\.0\\.height=(\\d+)");
            long durationMs = extractDurationMs(output);

            log.info("[PROBE] {}x{} {}ms file={}", width, height, durationMs, videoFile.getFileName());
            return new VideoMetadata(width, height, durationMs);

        } catch (IOException | InterruptedException e) {
            log.warn("[PROBE] failed for {}", videoFile, e);
            return new VideoMetadata(0, 0, 0);
        }
    }

    private int extractInt(String output, String regex) {
        Matcher m = Pattern.compile(regex).matcher(output);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private long extractDurationMs(String output) {
        // Try stream duration first, then format duration
        for (String regex : List.of(
                "streams\\.stream\\.0\\.duration=\"([\\d.]+)\"",
                "format\\.duration=\"([\\d.]+)\"")) {
            Matcher m = Pattern.compile(regex).matcher(output);
            if (m.find()) {
                return (long) (Double.parseDouble(m.group(1)) * 1000);
            }
        }
        return 0;
    }
}
