package com.aiott.ottpoc.application.dto.admin;

import java.util.UUID;

public record AdminGenreResult(
        UUID id,
        String slug,
        String label,
        String description,
        int sortOrder,
        boolean active
) {}
