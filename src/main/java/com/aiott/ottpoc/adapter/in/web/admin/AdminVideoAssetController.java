package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetDetail;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetSummary;
import com.aiott.ottpoc.application.port.in.AdminVideoAssetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/video-assets")
public class AdminVideoAssetController {

    private final AdminVideoAssetUseCase useCase;

    @GetMapping
    public List<AdminVideoAssetSummary> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return useCase.list(status, limit);
    }

    @GetMapping("/{videoAssetId}")
    public AdminVideoAssetDetail detail(@PathVariable UUID videoAssetId) {
        return useCase.get(videoAssetId);
    }
}
