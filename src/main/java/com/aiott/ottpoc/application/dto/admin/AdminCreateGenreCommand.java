package com.aiott.ottpoc.application.dto.admin;

public record AdminCreateGenreCommand(
        String slug,
        String label,
        String description,
        Integer sortOrder,
        Boolean active,
        String lang
) {}
