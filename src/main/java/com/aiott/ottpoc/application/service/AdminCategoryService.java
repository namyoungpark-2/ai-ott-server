package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateCategoryCommand;
import com.aiott.ottpoc.application.port.in.AdminCategoryUseCase;
import com.aiott.ottpoc.application.port.out.AdminCategoryCommandPort;
import com.aiott.ottpoc.application.port.out.AdminCategoryQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryService implements AdminCategoryUseCase {
    private final AdminCategoryCommandPort commandPort;
    private final AdminCategoryQueryPort queryPort;

    @Override
    @Transactional
    public AdminCategoryResult create(AdminCreateCategoryCommand command) {
        return commandPort.create(command);
    }

    @Override
    @Transactional
    public AdminCategoryResult update(String slug, AdminUpdateCategoryCommand command) {
        return commandPort.update(slug, command);
    }

    @Override
    @Transactional
    public void delete(String slug) {
        commandPort.delete(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCategoryResult> list() {
        return queryPort.list();
    }
}
