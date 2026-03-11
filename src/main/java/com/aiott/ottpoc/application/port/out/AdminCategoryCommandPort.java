package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;

public interface AdminCategoryCommandPort {
    AdminCategoryResult create(AdminCreateCategoryCommand command);
}
