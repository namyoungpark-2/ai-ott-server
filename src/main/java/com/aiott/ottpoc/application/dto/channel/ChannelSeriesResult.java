package com.aiott.ottpoc.application.dto.channel;

import java.util.UUID;

public record ChannelSeriesResult(
        UUID seriesId,
        String title,
        String status,
        int episodeCount
) {}
