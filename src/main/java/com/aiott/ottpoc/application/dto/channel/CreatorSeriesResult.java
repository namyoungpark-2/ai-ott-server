package com.aiott.ottpoc.application.dto.channel;

import java.util.UUID;

public record CreatorSeriesResult(
        UUID seriesId,
        String title,
        String description,
        int episodeCount
) {}
