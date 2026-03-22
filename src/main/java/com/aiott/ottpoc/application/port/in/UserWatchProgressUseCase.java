package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.WatchProgressItem;

import java.util.List;
import java.util.UUID;

public interface UserWatchProgressUseCase {

    /** Returns the user's in-progress content, most recently watched first. */
    List<WatchProgressItem> getContinueWatching(UUID userId, String lang);

    /** Returns the saved position in milliseconds, or 0 if not found. */
    long getPosition(UUID userId, UUID contentId);

    /** Upserts the watch position for a content item. */
    void saveProgress(UUID userId, UUID contentId, long positionMs, Long durationMs);
}
