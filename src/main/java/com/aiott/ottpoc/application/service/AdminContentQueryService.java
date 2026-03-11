package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminContentDetail;
import com.aiott.ottpoc.application.dto.admin.AdminContentSummary;
import com.aiott.ottpoc.application.port.in.AdminContentQueryUseCase;
import com.aiott.ottpoc.application.port.out.admin.AdminContentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentQueryService implements AdminContentQueryUseCase {

    private final AdminContentQueryPort queryPort;

    @Override
    public List<AdminContentSummary> list(String lang, String status, int limit) {
        return queryPort.list(lang, status, limit);
    }

    @Override
    public AdminContentDetail get(UUID contentId, String lang) {
        return queryPort.get(contentId, lang)
                .orElseThrow(() -> new IllegalArgumentException("content not found: " + contentId));
    }
}
