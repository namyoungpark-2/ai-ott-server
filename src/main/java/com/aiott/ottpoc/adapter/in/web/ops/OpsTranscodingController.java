package com.aiott.ottpoc.adapter.in.web.ops;

import com.aiott.ottpoc.application.port.in.OpsTranscodingUseCase;
import com.aiott.ottpoc.application.security.Permissions;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ops/transcoding")
public class OpsTranscodingController {

    private final OpsTranscodingUseCase useCase;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).METRICS_READ)")
    public Object summary() {
        return useCase.getSummary();
    }

    @GetMapping("/failures/top")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).METRICS_READ)")
    public Object failuresTop(@RequestParam(defaultValue = "5") int limit) {
        return useCase.getFailureTop(limit);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority(T(com.aiott.ottpoc.application.security.Permissions).METRICS_READ)")
    public Object recent(@RequestParam(defaultValue = "20") int limit) {
        return useCase.getRecent(limit);
    }
}
