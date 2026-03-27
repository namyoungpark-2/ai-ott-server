package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentMetadataCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateContentTaxonomyCommand;
import com.aiott.ottpoc.application.port.in.AdminContentMetadataUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/contents")
@RequiredArgsConstructor
public class AdminContentMetadataController {
    private final AdminContentMetadataUseCase useCase;

    @PutMapping("/{contentId}/metadata")

    public void updateMetadata(@PathVariable UUID contentId, @RequestBody AdminUpdateContentMetadataCommand command) {
        useCase.updateMetadata(contentId, command);
    }

    @PutMapping("/{contentId}/taxonomy")

    public void updateTaxonomy(@PathVariable UUID contentId, @RequestBody AdminUpdateContentTaxonomyCommand command) {
        useCase.updateTaxonomy(contentId, command);
    }
}
