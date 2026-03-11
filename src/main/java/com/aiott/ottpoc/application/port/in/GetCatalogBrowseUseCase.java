package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.CatalogBrowseResponse;

public interface GetCatalogBrowseUseCase {
    CatalogBrowseResponse getBrowse(String lang, int sectionLimit);
}
