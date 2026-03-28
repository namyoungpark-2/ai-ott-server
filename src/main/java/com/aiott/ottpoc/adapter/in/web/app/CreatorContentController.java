package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.channel.CreatorContentResult;
import com.aiott.ottpoc.application.dto.channel.CreatorContentSummary;
import com.aiott.ottpoc.application.dto.channel.CreatorCreateContentCommand;
import com.aiott.ottpoc.application.port.in.CreatorContentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/creator/contents")
public class CreatorContentController {
    private final CreatorContentUseCase useCase;

    @PostMapping
    public CreatorContentResult create(@RequestBody CreatorCreateContentCommand cmd) {
        return useCase.createContent(getUserId(), cmd);
    }

    @GetMapping
    public List<CreatorContentSummary> list(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "50") int limit) {
        return useCase.listMyContents(getUserId(), LangResolver.resolve(lang), limit);
    }

    @PutMapping("/{id}/metadata")
    public Map<String, String> updateMetadata(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String lang = body.getOrDefault("lang", "en");
        useCase.updateContentMetadata(getUserId(), id, body.get("title"), body.get("description"), lang);
        return Map.of("message", "updated");
    }

    @PatchMapping("/{id}/status")
    public Map<String, String> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        useCase.updateContentStatus(getUserId(), id, status);
        return Map.of("message", "updated");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable UUID id) {
        useCase.deleteContent(getUserId(), id);
        return Map.of("message", "deleted");
    }

    private String getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new SecurityException("Not authenticated");
    }
}
