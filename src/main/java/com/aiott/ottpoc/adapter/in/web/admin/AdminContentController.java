package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminAttachAssetResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentCommand;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentResult;
import com.aiott.ottpoc.application.port.in.AdminContentUseCase;
import com.aiott.ottpoc.application.port.in.AdminContentStatusUseCase;
import com.aiott.ottpoc.application.security.Permissions;
import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;
import com.aiott.ottpoc.application.port.in.AdminContentQueryUseCase;
import java.util.List;
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

    // ✅ B-1: content만 생성
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).CONTENT_CREATE)")
    public AdminCreateContentResult create(@RequestBody AdminCreateContentCommand cmd) {
        return useCase.createContent(cmd);
    }

    // ✅ B-2: 기존 content에 asset 붙이기
    @PostMapping("/{contentId}/assets")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).ASSET_CREATE)")
    public AdminAttachAssetResult attach(
            @PathVariable UUID contentId,
            @RequestPart("file") MultipartFile file
    ) {
        return useCase.attachAsset(contentId, file);
    }

    @PatchMapping("/{contentId}/status")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).CONTENT_PUBLISH)")
    public void changeStatus(
            @PathVariable UUID contentId,
            @RequestParam String status
    ) {
        adminContentStatusUseCase.changeStatus(contentId, status);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<AdminContentSummary> list(
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return queryUseCase.list(lang, status, limit);
    }

    @GetMapping("/{contentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AdminContentDetail detail(
            @PathVariable UUID contentId,
            @RequestParam(defaultValue = "en") String lang
    ) {
        return queryUseCase.get(contentId, lang);
    }


}
