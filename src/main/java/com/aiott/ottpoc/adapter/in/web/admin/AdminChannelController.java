package com.aiott.ottpoc.adapter.in.web.admin;

import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;
import com.aiott.ottpoc.application.port.in.AdminChannelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/channels")
@PreAuthorize("hasRole('ADMIN')")
public class AdminChannelController {
    private final AdminChannelUseCase useCase;

    @GetMapping
    public List<ChannelSummaryResult> list(@RequestParam(defaultValue = "50") int limit) {
        return useCase.listChannels(limit);
    }

    @PatchMapping("/{id}/status")
    public Map<String, String> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        useCase.updateChannelStatus(id, status);
        return Map.of("message", "updated");
    }
}
