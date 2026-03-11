package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentMetadataCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentTaxonomyCommand;

import java.util.UUID;

public interface AdminContentMetadataCommandPort {
    void updateMetadata(UUID contentId, AdminUpdateContentMetadataCommand command);
    void updateTaxonomy(UUID contentId, AdminUpdateContentTaxonomyCommand command);
}
