package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminAttachAssetResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentCommand;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentResult;
import com.aiott.ottpoc.application.port.in.AdminContentUseCase;
import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentService implements AdminContentUseCase {

    private final CatalogCommandPort catalogCommandPort;
    private final AssetStoragePort storagePort;
    private final MediaStoragePort mediaStoragePort;
    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase;

    @Override
    @Transactional
    public AdminCreateContentResult createContent(AdminCreateContentCommand cmd) {
        String mode = (cmd.mode() == null ? "MOVIE" : cmd.mode().toUpperCase());
        String title = (cmd.title() == null || cmd.title().isBlank()) ? "Untitled" : cmd.title();
        String defaultLang = "en";

        UUID contentId;

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

        return new AdminCreateContentResult(contentId);
    }

    @Override
    @Transactional
    public AdminAttachAssetResult attachAsset(UUID contentId, MultipartFile file) {
        try {
            var tempPath = storagePort.saveSourceVideo(file.getBytes(), file.getOriginalFilename());
            var sourceKey = mediaStoragePort.storeSource(tempPath, contentId, file.getOriginalFilename());

            var videoAssetId = videoAssetCommandPort.createVideoAsset(
                    contentId,
                    mediaStoragePort.storageType(),
                    sourceKey,
                    VideoAssetStatus.UPLOADED
            );

            transcodeVideoAssetUseCase.transcode(videoAssetId, null);

            return new AdminAttachAssetResult(contentId, videoAssetId, "PROCESSING");
        } catch (Exception e) {
            throw new RuntimeException("Attach asset failed", e);
        }
    }
}
