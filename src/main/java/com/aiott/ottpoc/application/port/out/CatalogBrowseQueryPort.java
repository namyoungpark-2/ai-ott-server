package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.CatalogBrowseItemResult;
import com.aiott.ottpoc.application.dto.CatalogCategoryResult;

import java.util.List;

public interface CatalogBrowseQueryPort {
    List<CatalogBrowseItemResult> loadFeatured(String lang, int limit);
    List<CatalogBrowseItemResult> loadLatestMovies(String lang, int limit);
    List<CatalogBrowseItemResult> loadLatestSeriesEpisodes(String lang, int limit);
    List<CatalogCategoryResult> loadActiveCategories();
    List<CatalogBrowseItemResult> loadByCategory(String lang, String categorySlug, int limit);
}
