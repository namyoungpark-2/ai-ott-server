package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.CatalogBrowseResponse;
import com.aiott.ottpoc.application.dto.CatalogBrowseSectionResult;
import com.aiott.ottpoc.application.port.in.GetCatalogBrowseUseCase;
import com.aiott.ottpoc.application.port.out.CatalogBrowseQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogBrowseService implements GetCatalogBrowseUseCase {
    private final CatalogBrowseQueryPort queryPort;

    @Override
    public CatalogBrowseResponse getBrowse(String lang, int sectionLimit) {
        List<CatalogBrowseSectionResult> sections = new ArrayList<>();
        sections.add(new CatalogBrowseSectionResult("featured", "Featured", queryPort.loadFeatured(lang, Math.min(sectionLimit, 6))));
        sections.add(new CatalogBrowseSectionResult("latest-movies", "Latest Movies", queryPort.loadLatestMovies(lang, sectionLimit)));
        sections.add(new CatalogBrowseSectionResult("latest-series", "Latest Episodes", queryPort.loadLatestSeriesEpisodes(lang, sectionLimit)));
        queryPort.loadActiveCategories().forEach(category -> {
            var items = queryPort.loadByCategory(lang, category.slug(), sectionLimit);
            if (!items.isEmpty()) {
                sections.add(new CatalogBrowseSectionResult(category.slug(), category.label(), items));
            }
        });
        return new CatalogBrowseResponse(sections);
    }
}
