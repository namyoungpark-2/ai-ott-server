package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentMetadataCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentTaxonomyCommand;

import java.util.UUID;

public interface AdminContentMetadataUseCase {
    void updateMetadata(UUID contentId, AdminUpdateContentMetadataCommand command);
    void updateTaxonomy(UUID contentId, AdminUpdateContentTaxonomyCommand command);
}
