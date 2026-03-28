package com.aiott.ottpoc.application.dto.channel;

import java.util.UUID;

public record ChannelSummaryResult(
        UUID id,
        String handle,
        String name,
        String profileImageUrl,
        boolean isOfficial,
        int subscriberCount,
        String status
) {}
