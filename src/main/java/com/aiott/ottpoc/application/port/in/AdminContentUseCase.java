package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminAttachAssetResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentCommand;
import com.aiott.ottpoc.application.dto.admin.AdminCreateContentResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AdminContentUseCase {
    AdminCreateContentResult createContent(AdminCreateContentCommand cmd);
    AdminAttachAssetResult attachAsset(UUID contentId, MultipartFile file);
}
