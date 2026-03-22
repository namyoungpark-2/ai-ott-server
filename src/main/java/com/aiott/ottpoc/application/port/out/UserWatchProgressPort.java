package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.WatchProgressItem;

import java.util.List;
import java.util.UUID;

public interface UserWatchProgressPort {

    List<WatchProgressItem> findContinueWatching(UUID userId, String lang, int limit);

    long findPosition(UUID userId, UUID contentId);

    void upsertProgress(UUID userId, UUID contentId, long positionMs, Long durationMs);
}
