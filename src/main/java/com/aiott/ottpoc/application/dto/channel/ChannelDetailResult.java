package com.aiott.ottpoc.application.dto.channel;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChannelDetailResult(
        UUID id,
        String handle,
        String name,
        String description,
        String profileImageUrl,
        String bannerImageUrl,
        boolean isOfficial,
        int subscriberCount,
        String status,
        OffsetDateTime createdAt
) {}
