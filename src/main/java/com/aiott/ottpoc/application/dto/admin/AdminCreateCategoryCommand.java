package com.aiott.ottpoc.application.dto.admin;

public record AdminCreateCategoryCommand(
        String slug,
        String label,
        String description,
        Integer sortOrder,
        Boolean active
) {}
