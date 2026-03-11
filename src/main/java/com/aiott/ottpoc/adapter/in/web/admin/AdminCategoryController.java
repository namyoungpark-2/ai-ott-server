package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.port.in.AdminCategoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final AdminCategoryUseCase useCase;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AdminCategoryResult create(@RequestBody AdminCreateCategoryCommand command) {
        return useCase.create(command);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<AdminCategoryResult> list() {
        return useCase.list();
    }
}
