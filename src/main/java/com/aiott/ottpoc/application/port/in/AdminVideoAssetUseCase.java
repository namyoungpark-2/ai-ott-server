package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetDetail;
import com.aiott.ottpoc.application.dto.admin.AdminVideoAssetSummary;

import java.util.List;
import java.util.UUID;

public interface AdminVideoAssetUseCase {
    List<AdminVideoAssetSummary> list(String status, int limit);
    AdminVideoAssetDetail get(UUID videoAssetId);
}
