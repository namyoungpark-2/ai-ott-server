package com.aiott.ottpoc.application.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record WatchEventIngestCommand(
        String clientEventId,
        String eventType,
        OffsetDateTime occurredAt,

        UUID contentId,
        UUID videoAssetId,

        String sessionId,
        String deviceId,

        int positionMs,
        Integer deltaMs,
        Integer durationMs,
        Double playbackRate,

        String country,
        String player,
        String appVersion,
        String networkType,

        Map<String, Object> extra
) {}
