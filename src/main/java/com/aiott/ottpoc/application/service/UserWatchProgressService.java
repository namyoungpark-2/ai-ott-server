package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.WatchProgressItem;
import com.aiott.ottpoc.application.port.in.UserWatchProgressUseCase;
import com.aiott.ottpoc.application.port.out.UserWatchProgressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserWatchProgressService implements UserWatchProgressUseCase {

    private final UserWatchProgressPort port;

    @Override
    public List<WatchProgressItem> getContinueWatching(UUID userId, String lang) {
        String effectiveLang = (lang == null || lang.isBlank()) ? "en" : lang;
        return port.findContinueWatching(userId, effectiveLang, 20);
    }

    @Override
    public long getPosition(UUID userId, UUID contentId) {
        return port.findPosition(userId, contentId);
    }

    @Override
    public void saveProgress(UUID userId, UUID contentId, long positionMs, Long durationMs) {
        if (positionMs < 0) positionMs = 0;
        port.upsertProgress(userId, contentId, positionMs, durationMs);
    }
}
