package com.aiott.ottpoc.application.dto;

import java.util.List;

public record CatalogBrowseResponse(
        List<CatalogBrowseSectionResult> sections,
        List<CatalogCategoryResult> categories,
        List<CatalogGenreResult> genres
) {}
