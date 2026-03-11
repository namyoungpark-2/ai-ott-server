package com.aiott.ottpoc.application.port.out.admin;

import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminContentQueryPort {
    List<AdminContentSummary> list(String lang, String status, int limit);
    Optional<AdminContentDetail> get(UUID contentId, String lang);
}
