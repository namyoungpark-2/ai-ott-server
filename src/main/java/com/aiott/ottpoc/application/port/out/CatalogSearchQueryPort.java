package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.CatalogBrowseItemResult;

import java.util.List;

public interface CatalogSearchQueryPort {
    List<CatalogBrowseItemResult> search(String lang, String query, String category, String genre, int limit, int offset);
    int count(String lang, String query, String category, String genre);
}
