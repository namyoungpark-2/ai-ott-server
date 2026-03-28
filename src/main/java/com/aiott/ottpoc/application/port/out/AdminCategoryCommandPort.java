package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateCategoryCommand;

public interface AdminCategoryCommandPort {
    AdminCategoryResult create(AdminCreateCategoryCommand command);
    AdminCategoryResult update(String slug, AdminUpdateCategoryCommand command);
    void delete(String slug);
}
