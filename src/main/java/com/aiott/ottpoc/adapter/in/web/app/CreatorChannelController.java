package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.channel.ChannelDetailResult;
import com.aiott.ottpoc.application.dto.channel.UpdateChannelCommand;
import com.aiott.ottpoc.application.port.in.CreatorChannelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/creator/channel")
public class CreatorChannelController {
    private final CreatorChannelUseCase useCase;

    @GetMapping
    public ChannelDetailResult getMyChannel(@RequestParam(required = false) String lang) {
        return useCase.getOrCreateMyChannel(getUserId(), LangResolver.resolve(lang));
    }

    @PutMapping
    public ChannelDetailResult updateMyChannel(@RequestBody UpdateChannelCommand cmd) {
        return useCase.updateMyChannel(getUserId(), cmd);
    }

    private String getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new SecurityException("Not authenticated");
    }
}
