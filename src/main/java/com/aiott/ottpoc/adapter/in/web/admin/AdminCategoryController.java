package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.admin.AdminCategoryResult;
import com.aiott.ottpoc.application.dto.admin.AdminCreateCategoryCommand;
import com.aiott.ottpoc.application.dto.admin.AdminUpdateCategoryCommand;
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
    public AdminCategoryResult create(@RequestBody AdminCreateCategoryCommand command) {
        return useCase.create(command);
    }

    @PutMapping("/{slug}")
    public AdminCategoryResult update(@PathVariable String slug,
                                      @RequestBody AdminUpdateCategoryCommand command) {
        return useCase.update(slug, command);
    }

    @DeleteMapping("/{slug}")
    public void delete(@PathVariable String slug) {
        useCase.delete(slug);
    }

    @GetMapping
    public List<AdminCategoryResult> list() {
        return useCase.list();
    }
}
