package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateCategoryCommand;

import java.util.List;

public interface AdminCategoryUseCase {
    AdminCategoryResult create(AdminCreateCategoryCommand command);
    AdminCategoryResult update(String slug, AdminUpdateCategoryCommand command);
    void delete(String slug);
    List<AdminCategoryResult> list();
}
