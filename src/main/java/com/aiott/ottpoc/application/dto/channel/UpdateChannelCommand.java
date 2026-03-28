package com.aiott.ottpoc.application.dto.channel;

public record UpdateChannelCommand(
        String name,
        String description,
        String profileImageUrl,
        String bannerImageUrl
) {}
