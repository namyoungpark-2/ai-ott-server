package com.aiott.ottpoc.application.dto;

public record CatalogGenreResult(
        String slug,
        String label,
        String description,
        int sortOrder
) {}
