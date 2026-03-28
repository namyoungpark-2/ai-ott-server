package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record ContentViewResult(
        UUID id,
        String title,
        String status,        // PROCESSING | READY | FAILED
        String streamUrl,     // READY일 때만
        String thumbnailUrl,
        String errorMessage,
        Integer videoWidth,
        Integer videoHeight,
        String orientation,
        Long durationMs,
        String channelHandle,
        String channelName
) {}
