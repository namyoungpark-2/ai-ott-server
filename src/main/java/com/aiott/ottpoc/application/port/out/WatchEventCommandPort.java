package com.aiott.ottpoc.application.port.out;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface WatchEventCommandPort {

    record WatchEventToStore(
            String clientEventId,
            String eventType,
            OffsetDateTime occurredAt,

            UUID contentId,
            UUID videoAssetId,

            String sessionId,
            String deviceId,

            int positionMs,
            Integer deltaMs,
            Integer durationMs,
            Double playbackRate,

            String country,
            String player,
            String appVersion,
            String networkType,

            String extraJson
    ) {}

    /**
     * @return 저장된 eventId (중복이면 empty)
     */
    Optional<UUID> storeRaw(WatchEventToStore e);

    /**
     * @return true면 "그날-콘텐츠" 조합에서 device가 처음 등장(유니크 증가)
     */
    boolean ensureDailyUniqueDevice(LocalDate aggDate, UUID contentId, String deviceId);

    void upsertDailyAgg(LocalDate aggDate, UUID contentId, int playInc, int uniqueDeviceInc, long watchTimeIncMs, int completeInc);
}
