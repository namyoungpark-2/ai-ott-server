package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.CatalogBrowseResponse;
import com.aiott.ottpoc.application.dto.CatalogBrowseSectionResult;
import com.aiott.ottpoc.application.port.in.GetCatalogBrowseUseCase;
import com.aiott.ottpoc.application.port.out.CatalogBrowseQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogBrowseService implements GetCatalogBrowseUseCase {
    private final CatalogBrowseQueryPort queryPort;

    private static final Map<String, Map<String, String>> SECTION_TITLES = Map.of(
        "featured",       Map.of("ko", "추천 콘텐츠",  "en", "Featured"),
        "latest-movies",  Map.of("ko", "최신 콘텐츠",  "en", "Latest Movies"),
        "latest-series",  Map.of("ko", "최신 에피소드", "en", "Latest Episodes"),
        "popular",        Map.of("ko", "인기 콘텐츠",  "en", "Popular"),
        "trending",       Map.of("ko", "트렌딩",      "en", "Trending"),
        "movies",         Map.of("ko", "영화",        "en", "Movies"),
        "television",     Map.of("ko", "TV 프로그램",  "en", "Television"),
        "documentary",    Map.of("ko", "다큐멘터리",   "en", "Documentary"),
        "entertainment",  Map.of("ko", "엔터테인먼트", "en", "Entertainment"),
        "animation",      Map.of("ko", "애니메이션",   "en", "Animation")
    );

    private String resolveTitle(String key, String lang, String fallback) {
        var titles = SECTION_TITLES.get(key);
        if (titles == null) return fallback;
        String title = titles.get(lang);
        return title != null ? title : titles.getOrDefault("en", fallback);
    }

    @Override
    public CatalogBrowseResponse getBrowse(String lang, int sectionLimit) {
        var categories = queryPort.loadActiveCategories(lang);
        var genres = queryPort.loadActiveGenres(lang);

        List<CatalogBrowseSectionResult> sections = new ArrayList<>();
        sections.add(new CatalogBrowseSectionResult("featured", resolveTitle("featured", lang, "Featured"), queryPort.loadFeatured(lang, Math.min(sectionLimit, 6))));
        sections.add(new CatalogBrowseSectionResult("latest-movies", resolveTitle("latest-movies", lang, "Latest Movies"), queryPort.loadLatestMovies(lang, sectionLimit)));
        sections.add(new CatalogBrowseSectionResult("latest-series", resolveTitle("latest-series", lang, "Latest Episodes"), queryPort.loadLatestSeriesEpisodes(lang, sectionLimit)));

        // 카테고리별 섹션 — category.label()은 이미 i18n 적용됨
        categories.forEach(category -> {
            var items = queryPort.loadByCategory(lang, category.slug(), sectionLimit);
            if (!items.isEmpty()) {
                String title = resolveTitle(category.slug(), lang, category.label());
                sections.add(new CatalogBrowseSectionResult(category.slug(), title, items));
            }
        });

        return new CatalogBrowseResponse(sections, categories, genres);
    }
}
