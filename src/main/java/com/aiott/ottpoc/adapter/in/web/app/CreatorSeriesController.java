package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.channel.CreatorCreateSeriesCommand;
import com.aiott.ottpoc.application.dto.channel.CreatorSeriesResult;
import com.aiott.ottpoc.application.port.in.CreatorSeriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/creator/series")
public class CreatorSeriesController {
    private final CreatorSeriesUseCase useCase;

    @PostMapping
    public CreatorSeriesResult create(@RequestBody CreatorCreateSeriesCommand cmd) {
        return useCase.createSeries(getUserId(), cmd);
    }

    @GetMapping
    public List<CreatorSeriesResult> list(@RequestParam(required = false) String lang) {
        return useCase.listMySeries(getUserId(), LangResolver.resolve(lang));
    }

    @PutMapping("/{id}")
    public Map<String, String> update(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String lang = body.getOrDefault("lang", "en");
        useCase.updateSeries(getUserId(), id, body.get("title"), body.get("description"), lang);
        return Map.of("message", "updated");
    }

    private String getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new SecurityException("Not authenticated");
    }
}
