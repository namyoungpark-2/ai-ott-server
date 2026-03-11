package com.aiott.ottpoc.application.port.out.admin;

import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetDetail;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminVideoAssetQueryPort {
    List<AdminVideoAssetSummary> findVideoAssets(String status, int limit);
    Optional<AdminVideoAssetDetail> findVideoAsset(UUID videoAssetId);
}
