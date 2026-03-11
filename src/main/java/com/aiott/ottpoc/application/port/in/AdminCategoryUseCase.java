package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;

import java.util.List;

public interface AdminCategoryUseCase {
    AdminCategoryResult create(AdminCreateCategoryCommand command);
    List<AdminCategoryResult> list();
}
