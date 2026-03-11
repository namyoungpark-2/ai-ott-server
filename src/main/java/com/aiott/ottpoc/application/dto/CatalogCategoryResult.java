package com.aiott.ottpoc.application.dto;

public record CatalogCategoryResult(
        String slug,
        String label,
        String description,
        int sortOrder
) {}
