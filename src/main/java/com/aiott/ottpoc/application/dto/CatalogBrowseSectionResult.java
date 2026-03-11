package com.aiott.ottpoc.application.dto;

import java.util.List;

public record CatalogBrowseSectionResult(
        String key,
        String title,
        List<CatalogBrowseItemResult> items
) {}
