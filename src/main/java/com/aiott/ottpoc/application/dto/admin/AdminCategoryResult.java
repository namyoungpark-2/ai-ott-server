package com.aiott.ottpoc.application.dto.admin;

import java.util.UUID;

public record AdminCategoryResult(
        UUID id,
        String slug,
        String label,
        String description,
        int sortOrder,
        boolean active
) {}
