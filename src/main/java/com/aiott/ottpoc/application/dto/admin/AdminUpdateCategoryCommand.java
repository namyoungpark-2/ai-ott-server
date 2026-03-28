package com.aiott.ottpoc.application.dto.admin;

public record AdminUpdateCategoryCommand(
        String label,
        String description,
        Integer sortOrder,
        Boolean active,
        String iabCode,
        Integer tier,
        String parentSlug
) {}
