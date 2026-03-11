package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record SeasonResult(
        UUID id,
        int seasonNumber,
        String title,
        String description
) {}
