package com.aiott.ottpoc.application.dto.admin;

import java.util.UUID;

public record AdminAttachAssetResult(
        UUID contentId,
        UUID videoAssetId,
        String status
) {}
