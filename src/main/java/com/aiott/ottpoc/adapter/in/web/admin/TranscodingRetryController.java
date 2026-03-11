package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.port.in.RetryTranscodingUseCase;
import com.aiott.ottpoc.application.security.Permissions;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/video-assets")
public class TranscodingRetryController {

    private final RetryTranscodingUseCase useCase;

    @PostMapping("/{videoAssetId}/retry")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).JOB_RETRY)")
    public RetryTranscodingUseCase.RetryResult retry(@PathVariable UUID videoAssetId) {
        return useCase.retry(videoAssetId);
    }
}
