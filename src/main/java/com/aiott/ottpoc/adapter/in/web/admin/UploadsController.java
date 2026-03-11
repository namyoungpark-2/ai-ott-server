package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.UnifiedUploadCommand;
import com.aiott.ottpoc.application.dto.UnifiedUploadResult;
import com.aiott.ottpoc.application.port.in.UnifiedUploadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/uploads")
public class UploadsController {

    private final UnifiedUploadUseCase unifiedUploadUseCase;

    @PostMapping("/uploads")
    public UnifiedUploadResult upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value="contentId", required=false) String contentId,
            @RequestPart(value="title", required=false) String title,
            @RequestPart(value="mode", required=false) String mode,
            @RequestPart(value="seriesId", required=false) String seriesId,
            @RequestPart(value="seriesTitle", required=false) String seriesTitle,
            @RequestPart(value="seasonNumber", required=false) Integer seasonNumber,
            @RequestPart(value="episodeNumber", required=false) Integer episodeNumber
    ) {
        UnifiedUploadCommand cmd = new UnifiedUploadCommand(
                contentId == null || contentId.isBlank() ? null : UUID.fromString(contentId),
                mode,
                title,
                seriesId == null || seriesId.isBlank() ? null : UUID.fromString(seriesId),
                seriesTitle,
                seasonNumber,
                episodeNumber
        );
        return unifiedUploadUseCase.upload(file, cmd);
    }
}
