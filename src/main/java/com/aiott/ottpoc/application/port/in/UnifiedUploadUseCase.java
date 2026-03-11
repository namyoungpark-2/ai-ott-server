package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.UnifiedUploadCommand;
import com.aiott.ottpoc.application.dto.UnifiedUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface UnifiedUploadUseCase {
    UnifiedUploadResult upload(MultipartFile file, UnifiedUploadCommand cmd);
}
