package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record UnifiedUploadResult(
        UUID id,          // contentId (프론트와 계약)
        String status     // TRANSCODING / READY / FAILED
) {}


