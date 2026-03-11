package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetDetail;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetSummary;
import com.aiott.ottpoc.application.port.in.AdminVideoAssetUseCase;
import com.aiott.ottpoc.application.port.out.admin.AdminVideoAssetQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminVideoAssetService implements AdminVideoAssetUseCase {

    private final AdminVideoAssetQueryPort queryPort;

    @Override
    public List<AdminVideoAssetSummary> list(String status, int limit) {
        return queryPort.findVideoAssets(status, limit);
    }

    @Override
    public AdminVideoAssetDetail get(UUID videoAssetId) {
        return queryPort.findVideoAsset(videoAssetId)
                .orElseThrow(() -> new IllegalArgumentException("videoAsset not found: " + videoAssetId));
    }
}
