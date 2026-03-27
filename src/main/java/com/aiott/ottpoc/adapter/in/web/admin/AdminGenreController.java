package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminCreateGenreCommand;
import com.aiott.ottpoc.application.dto.admin.AdminGenreResult;
import com.aiott.ottpoc.application.port.in.AdminGenreUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {
    private final AdminGenreUseCase useCase;

    @PostMapping
    public AdminGenreResult create(@RequestBody AdminCreateGenreCommand command) {
        return useCase.create(command);
    }

    @GetMapping
    public List<AdminGenreResult> list() {
        return useCase.list();
    }
}
