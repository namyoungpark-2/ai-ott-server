package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminCreateGenreCommand;
import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;

public interface AdminGenreCommandPort {
    AdminGenreResult create(AdminCreateGenreCommand command);
}
