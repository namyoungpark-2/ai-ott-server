package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.CatalogBrowseResponse;
import com.aiott.ottpoc.application.dto.CatalogSearchResponse;
import com.aiott.ottpoc.application.port.in.GetCatalogBrowseUseCase;
import com.aiott.ottpoc.application.port.in.SearchCatalogUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final GetCatalogBrowseUseCase getCatalogBrowseUseCase;
    private final SearchCatalogUseCase searchCatalogUseCase;

    @GetMapping("/browse")
    public CatalogBrowseResponse browse(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "12") int sectionLimit
    ) {
        return getCatalogBrowseUseCase.getBrowse(LangResolver.resolve(lang), Math.max(1, Math.min(sectionLimit, 30)));
    }

    @GetMapping("/search")
    public CatalogSearchResponse search(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return searchCatalogUseCase.search(LangResolver.resolve(lang), q, category, genre, Math.max(1, Math.min(limit, 100)), Math.max(0, offset));
    }
}
