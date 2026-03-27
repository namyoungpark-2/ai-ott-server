package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminCreateGenreCommand;
import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;

import java.util.List;

public interface AdminGenreUseCase {
    AdminGenreResult create(AdminCreateGenreCommand command);
    List<AdminGenreResult> list();
}
