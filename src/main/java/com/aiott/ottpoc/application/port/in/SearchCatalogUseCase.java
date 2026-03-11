package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.CatalogSearchResponse;

public interface SearchCatalogUseCase {
    CatalogSearchResponse search(String lang, String query, String category, int limit, int offset);
}
