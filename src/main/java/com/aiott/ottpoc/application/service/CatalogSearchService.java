package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.CatalogSearchResponse;
import com.aiott.ottpoc.application.port.in.SearchCatalogUseCase;
import com.aiott.ottpoc.application.port.out.CatalogSearchQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogSearchService implements SearchCatalogUseCase {
    private final CatalogSearchQueryPort queryPort;

    @Override
    public CatalogSearchResponse search(String lang, String query, String category, String genre, int limit, int offset) {
        String normalized = query == null ? "" : query.trim();
        return new CatalogSearchResponse(
                normalized,
                queryPort.count(lang, normalized, category, genre),
                queryPort.search(lang, normalized, category, genre, limit, offset)
        );
    }
}
