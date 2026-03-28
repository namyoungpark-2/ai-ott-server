package com.aiott.ottpoc.application.dto.channel;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChannelContentResult(
        UUID contentId,
        String title,
        String contentType,
        String status,
        String thumbnailUrl,
        OffsetDateTime createdAt
) {}
