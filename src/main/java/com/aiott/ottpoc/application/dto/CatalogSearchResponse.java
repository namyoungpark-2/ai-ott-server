package com.aiott.ottpoc.application.dto;

import java.util.List;

public record CatalogSearchResponse(
        String query,
        int total,
        List<CatalogBrowseItemResult> items
) {}
