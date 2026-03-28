package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.UnifiedUploadUseCase;
import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import com.aiott.ottpoc.application.port.out.ChannelQueryPort;
import com.aiott.ottpoc.application.port.out.AssetStoragePort;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.application.dto.UnifiedUploadResult;
import com.aiott.ottpoc.application.dto.UnifiedUploadCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import com.aiott.ottpoc.domain.model.VideoAssetStatus;

@Service
@RequiredArgsConstructor
public class UnifiedUploadService implements UnifiedUploadUseCase {

    private final CatalogCommandPort catalogCommandPort;
    private final ChannelQueryPort channelQueryPort;
    private final AssetStoragePort storagePort;
    private final MediaStoragePort mediaStoragePort;
    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase;

    @Override
    public UnifiedUploadResult upload(MultipartFile file, UnifiedUploadCommand cmd) {
        try {
            UUID contentId = cmd.contentId();

            if (contentId == null) {
                UUID officialChannelId = channelQueryPort.findOfficialChannelId()
                        .orElseThrow(() -> new IllegalStateException("Official channel not found"));

                String mode = (cmd.mode() == null ? "MOVIE" : cmd.mode().toUpperCase());
                String title = (cmd.title() == null || cmd.title().isBlank()) ? "Untitled" : cmd.title();
                String defaultLang = "en";

                if ("EPISODE".equals(mode)) {
                    UUID seriesId = cmd.seriesId();
                    if (seriesId == null) {
                        String seriesTitle = (cmd.seriesTitle() == null || cmd.seriesTitle().isBlank())
                                ? "Untitled Series" : cmd.seriesTitle();
                        seriesId = catalogCommandPort.createSeriesWithChannel(seriesTitle, defaultLang, officialChannelId);
                    }

                    int seasonNumber = (cmd.seasonNumber() == null ? 1 : cmd.seasonNumber());
                    UUID seasonId = catalogCommandPort.ensureSeason(seriesId, seasonNumber, defaultLang);

                    int episodeNumber = (cmd.episodeNumber() == null)
                            ? catalogCommandPort.nextEpisodeNumber(seasonId)
                            : cmd.episodeNumber();

                    contentId = catalogCommandPort.createEpisodeContent(seriesId, seasonId, episodeNumber, title);

                } else {
                    contentId = catalogCommandPort.createMovieContentWithChannel(title, officialChannelId);
                }
            }

            // 1. Save to local temp
            var tempPath = storagePort.saveSourceVideo(file.getBytes(), file.getOriginalFilename());

            // 2. Store to permanent storage (local: no-op, R2: upload + delete temp)
            var sourceKey = mediaStoragePort.storeSource(tempPath, contentId, file.getOriginalFilename());

            // 3. Create VideoAsset record with the correct storage type
            var videoAssetId = videoAssetCommandPort.createVideoAsset(
                    contentId,
                    mediaStoragePort.storageType(),
                    sourceKey,
                    VideoAssetStatus.UPLOADED
            );

            // 4. Trigger async transcoding
            transcodeVideoAssetUseCase.transcode(videoAssetId, null);

            return new UnifiedUploadResult(contentId, "PROCESSING");

        } catch (Exception e) {
            throw new RuntimeException("Unified upload failed", e);
        }
    }
}
