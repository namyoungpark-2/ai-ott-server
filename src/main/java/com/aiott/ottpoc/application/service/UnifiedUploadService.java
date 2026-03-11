package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.UnifiedUploadUseCase;
import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.application.port.out.TranscodingPort;
import com.aiott.ottpoc.application.dto.UnifiedUploadResult;
import com.aiott.ottpoc.application.dto.UnifiedUploadCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;
import com.aiott.ottpoc.domain.model.StorageType;

@Service
@RequiredArgsConstructor
public class UnifiedUploadService implements UnifiedUploadUseCase {

    private final CatalogCommandPort catalogCommandPort;
    private final AssetStoragePort storagePort;
    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodingPort transcodingPort;
    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase; 

    @Override
    public UnifiedUploadResult upload(MultipartFile file, UnifiedUploadCommand cmd) {
        try {
            UUID contentId = cmd.contentId();

            // ✅ B 플로우: 기존 Content에 asset만 붙이기
            if (contentId == null) {
                // ✅ A 플로우: Content + Asset 같이 생성
                String mode = (cmd.mode() == null ? "MOVIE" : cmd.mode().toUpperCase());
                String title = (cmd.title() == null || cmd.title().isBlank()) ? "Untitled" : cmd.title();
                String defaultLang = "en";

                if ("EPISODE".equals(mode)) {
                    UUID seriesId = cmd.seriesId();
                    if (seriesId == null) {
                        String seriesTitle = (cmd.seriesTitle() == null || cmd.seriesTitle().isBlank())
                                ? "Untitled Series" : cmd.seriesTitle();
                        seriesId = catalogCommandPort.createSeries(seriesTitle, defaultLang);
                    }

                    int seasonNumber = (cmd.seasonNumber() == null ? 1 : cmd.seasonNumber());
                    UUID seasonId = catalogCommandPort.ensureSeason(seriesId, seasonNumber, defaultLang);

                    int episodeNumber = (cmd.episodeNumber() == null)
                            ? catalogCommandPort.nextEpisodeNumber(seasonId)
                            : cmd.episodeNumber();

                    contentId = catalogCommandPort.createEpisodeContent(seriesId, seasonId, episodeNumber, title);

                } else {
                    contentId = catalogCommandPort.createMovieContent(title);
                }
            }

            // 이후 로직은 동일 (원본 저장 -> video_asset 생성 -> transcode)
            var sourcePath = storagePort.saveSourceVideo(file.getBytes(), file.getOriginalFilename());

            var videoAssetId = videoAssetCommandPort.createVideoAsset(
                    contentId,
                    StorageType.LOCAL,
                    sourcePath.toString(),
                    VideoAssetStatus.UPLOADED
            );

            transcodeVideoAssetUseCase.transcode(videoAssetId, null);

            return new UnifiedUploadResult(contentId, "PROCESSING");


        } catch (Exception e) {
            throw new RuntimeException("Unified upload failed", e);
        }
    }
}
