package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/video-assets")
@PreAuthorize("hasRole('ADMIN')")
public class TranscodingController {

    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase;

    @PostMapping("/{videoAssetId}/transcode")
    public void transcode(@PathVariable UUID videoAssetId) {
        transcodeVideoAssetUseCase.transcode(videoAssetId, null);
    }
}
