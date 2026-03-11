package com.aiott.ottpoc.adapter.out.transcoding;

import com.aiott.ottpoc.application.port.out.TranscodingPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class FfmpegLocalAdapter implements TranscodingPort {

    private final String ffmpegPath;

    public FfmpegLocalAdapter(@Value("${app.ffmpeg.path:ffmpeg}") String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    @Override
    public void transcodeToHls(Path sourceMp4, Path outputDir) {
        // 간단한 HLS 생성 (MVP)
        // master.m3u8 / index.m3u8 / segments 생성
        var master = outputDir.resolve("master.m3u8");
        var playlist = outputDir.resolve("index.m3u8");

        List<String> cmd = List.of(
                ffmpegPath,
                "-y",
                "-i", sourceMp4.toString(),
                "-profile:v", "main",
                "-vf", "scale=w=1280:h=-2",
                "-c:v", "h264",
                "-c:a", "aac",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", outputDir.resolve("seg_%03d.ts").toString(),
                playlist.toString()
        );

        run(cmd);

        // master는 MVP에선 playlist를 그대로 master로 복사해두자(다음 단계에서 ABR로 확장)
        try {
            java.nio.file.Files.copy(playlist, master, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create master.m3u8", e);
        }
    }

    @Override
    public void createThumbnail(Path sourceMp4, Path thumbnailPath) {
        List<String> cmd = List.of(
                ffmpegPath,
                "-y",
                "-i", sourceMp4.toString(),
                "-ss", "00:00:01.000",
                "-vframes", "1",
                thumbnailPath.toString()
        );
        run(cmd);
    }

    private void run(List<String> cmd) {
        try {
            var p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            int code = p.waitFor();
            if (code != 0) {
                var out = new String(p.getInputStream().readAllBytes());
                throw new RuntimeException("ffmpeg failed code=" + code + " output=" + out);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
