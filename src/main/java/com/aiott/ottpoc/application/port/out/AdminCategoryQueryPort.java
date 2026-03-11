package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;

import java.util.List;

public interface AdminCategoryQueryPort {
    List<AdminCategoryResult> list();
}
