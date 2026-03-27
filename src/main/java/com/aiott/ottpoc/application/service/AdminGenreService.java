package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.admin.AdminCreateGenreCommand;
import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;
import com.aiott.ottpoc.application.port.in.AdminGenreUseCase;
import com.aiott.ottpoc.application.port.out.AdminGenreCommandPort;
import com.aiott.ottpoc.application.port.out.AdminGenreQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminGenreService implements AdminGenreUseCase {
    private final AdminGenreCommandPort commandPort;
    private final AdminGenreQueryPort queryPort;

    @Override
    @Transactional
    public AdminGenreResult create(AdminCreateGenreCommand command) {
        return commandPort.create(command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminGenreResult> list() {
        return queryPort.list();
    }
}
