package com.aiott.ottpoc.application.dto;

import java.util.List;
import java.util.UUID;

public record SeriesDetailResult(
        SeriesMeta meta,
        List<SeasonResult> seasons
) {

    public record SeriesMeta(
            UUID id,
            String title,
            String description,
            String status,
            String defaultLanguage
    ) {}
}
