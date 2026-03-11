package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentMetadataCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentTaxonomyCommand;
import com.aiott.ottpoc.application.port.in.AdminContentMetadataUseCase;
import com.aiott.ottpoc.application.port.out.AdminContentMetadataCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentMetadataService implements AdminContentMetadataUseCase {
    private final AdminContentMetadataCommandPort commandPort;

    @Override
    @Transactional
    public void updateMetadata(UUID contentId, AdminUpdateContentMetadataCommand command) {
        commandPort.updateMetadata(contentId, command);
    }

    @Override
    @Transactional
    public void updateTaxonomy(UUID contentId, AdminUpdateContentTaxonomyCommand command) {
        commandPort.updateTaxonomy(contentId, command);
    }
}
