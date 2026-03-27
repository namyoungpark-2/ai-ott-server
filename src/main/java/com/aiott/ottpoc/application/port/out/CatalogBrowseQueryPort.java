package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.CatalogBrowseItemResult;
import com.aiott.ottpoc.application.dto.CatalogCategoryResult;
import com.aiott.ottpoc.application.dto.CatalogGenreResult;

import java.util.List;

public interface CatalogBrowseQueryPort {
    List<CatalogBrowseItemResult> loadFeatured(String lang, int limit);
    List<CatalogBrowseItemResult> loadLatestMovies(String lang, int limit);
    List<CatalogBrowseItemResult> loadLatestSeriesEpisodes(String lang, int limit);
    List<CatalogCategoryResult> loadActiveCategories(String lang);
    List<CatalogGenreResult> loadActiveGenres(String lang);
    List<CatalogBrowseItemResult> loadByCategory(String lang, String categorySlug, int limit);
    List<CatalogBrowseItemResult> loadByGenre(String lang, String genreSlug, int limit);
}
