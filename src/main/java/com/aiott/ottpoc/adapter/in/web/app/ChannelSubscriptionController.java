package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.channel.ChannelSummaryResult;
import com.aiott.ottpoc.application.port.in.ChannelSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChannelSubscriptionController {
    private final ChannelSubscriptionUseCase useCase;

    @PostMapping("/api/app/channels/{handle}/subscribe")
    public Map<String, String> subscribe(@PathVariable String handle) {
        useCase.subscribe(getUserId(), handle);
        return Map.of("message", "subscribed");
    }

    @DeleteMapping("/api/app/channels/{handle}/subscribe")
    public Map<String, String> unsubscribe(@PathVariable String handle) {
        useCase.unsubscribe(getUserId(), handle);
        return Map.of("message", "unsubscribed");
    }

    @GetMapping("/api/app/channels/{handle}/subscription-status")
    public Map<String, Boolean> subscriptionStatus(@PathVariable String handle) {
        boolean subscribed = useCase.isSubscribed(getUserId(), handle);
        return Map.of("subscribed", subscribed);
    }

    @GetMapping("/api/app/me/subscriptions")
    public List<ChannelSummaryResult> mySubscriptions(@RequestParam(required = false) String lang) {
        return useCase.listMySubscriptions(getUserId(), LangResolver.resolve(lang));
    }

    private String getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new SecurityException("Not authenticated");
    }
}
