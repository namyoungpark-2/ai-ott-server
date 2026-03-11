package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;

import java.util.List;
import java.util.UUID;

public interface AdminContentQueryUseCase {
    List<AdminContentSummary> list(String lang, String status, int limit);
    AdminContentDetail get(UUID contentId, String lang);
}
