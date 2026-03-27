package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;

import java.util.List;

public interface AdminGenreQueryPort {
    List<AdminGenreResult> list();
}
