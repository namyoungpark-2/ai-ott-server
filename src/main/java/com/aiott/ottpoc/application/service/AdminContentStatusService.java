package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.port.in.AdminContentStatusUseCase;
import com.aiott.ottpoc.application.port.out.CatalogCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentStatusService implements AdminContentStatusUseCase {

    private final CatalogCommandPort catalogCommandPort;

    @Override
    @Transactional
    public void changeStatus(UUID contentId, String status) {
        String s = status.toUpperCase();

        if (!s.equals("DRAFT") && !s.equals("PUBLISHED") && !s.equals("ARCHIVED")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        catalogCommandPort.updateContentStatus(contentId, s);
    }
}
