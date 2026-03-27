package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminAttachAssetResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentCommand;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentResult;
import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.port.in.AdminContentUseCase;
import com.aiott.ottpoc.application.port.in.AdminContentStatusUseCase;
import com.aiott.ottpoc.application.port.in.TranscodeVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;
import com.aiott.ottpoc.application.port.in.AdminContentQueryUseCase;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/contents")
public class AdminContentController {

    private final AdminContentUseCase useCase;
    private final AdminContentStatusUseCase adminContentStatusUseCase;
    private final AdminContentQueryUseCase queryUseCase;
    private final VideoAssetCommandPort videoAssetCommandPort;
    private final TranscodeVideoAssetUseCase transcodeVideoAssetUseCase;

    // ✅ B-1: content만 생성
    @PostMapping

    public AdminCreateContentResult create(@RequestBody AdminCreateContentCommand cmd) {
        return useCase.createContent(cmd);
    }

    // ✅ B-2: 기존 content에 asset 붙이기
    @PostMapping("/{contentId}/assets")

    public AdminAttachAssetResult attach(
            @PathVariable UUID contentId,
            @RequestPart("file") MultipartFile file
    ) {
        return useCase.attachAsset(contentId, file);
    }

    @PatchMapping("/{contentId}/status")

    public void changeStatus(
            @PathVariable UUID contentId,
            @RequestParam String status
    ) {
        adminContentStatusUseCase.changeStatus(contentId, status);
    }

    @GetMapping

    public List<AdminContentSummary> list(
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return queryUseCase.list(LangResolver.resolve(lang), status, limit);
    }

    @GetMapping("/{contentId}")

    public AdminContentDetail detail(
            @PathVariable UUID contentId,
            @RequestParam(required = false) String lang
    ) {
        return queryUseCase.get(contentId, LangResolver.resolve(lang));
    }

    @PostMapping("/{contentId}/transcode")

    public ResponseEntity<Void> transcode(@PathVariable UUID contentId) {
        var asset = videoAssetCommandPort.findLatestByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("No video asset found for content: " + contentId));
        transcodeVideoAssetUseCase.transcode(asset.id(), null);
        return ResponseEntity.accepted().build();
    }
}
