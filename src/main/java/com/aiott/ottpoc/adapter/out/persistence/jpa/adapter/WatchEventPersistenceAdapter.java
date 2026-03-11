package com.aiott.ottpoc.adapter.out.persistence.jpa.adapter;

import com.aiott.ottpoc.adapter.out.persistence.jpa.entity.WatchEventJpaEntity;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.WatchAggContentDailyJpaRepository;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.WatchDailyDeviceJpaRepository;
import com.aiott.ottpoc.adapter.out.persistence.jpa.repository.WatchEventJpaRepository;
import com.aiott.ottpoc.application.port.out.WatchAnalyticsQueryPort;
import com.aiott.ottpoc.application.port.out.WatchEventCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchEventPersistenceAdapter implements WatchEventCommandPort, WatchAnalyticsQueryPort {

    private final WatchEventJpaRepository eventRepo;
    private final WatchDailyDeviceJpaRepository dailyDeviceRepo;
    private final WatchAggContentDailyJpaRepository aggRepo;

    @Override
    public Optional<UUID> storeRaw(WatchEventToStore e) {
        try {
            var ent = new WatchEventJpaEntity();
            ent.setId(UUID.randomUUID());
            ent.setClientEventId(e.clientEventId());
            ent.setEventType(e.eventType());
            ent.setOccurredAt(e.occurredAt());
            ent.setReceivedAt(OffsetDateTime.now());

            ent.setContentId(e.contentId());
            ent.setVideoAssetId(e.videoAssetId());

            ent.setSessionId(e.sessionId());
            ent.setDeviceId(e.deviceId());

            ent.setPositionMs(e.positionMs());
            ent.setDeltaMs(e.deltaMs());
            ent.setDurationMs(e.durationMs());
            ent.setPlaybackRate(e.playbackRate());

            ent.setCountry(e.country());
            ent.setPlayer(e.player());
            ent.setAppVersion(e.appVersion());
            ent.setNetworkType(e.networkType());

            ent.setExtraJson(e.extraJson());

            eventRepo.save(ent);
            return Optional.of(ent.getId());
        } catch (DataIntegrityViolationException dup) {
            return Optional.empty();
        }
    }

    @Override
    public boolean ensureDailyUniqueDevice(LocalDate aggDate, UUID contentId, String deviceId) {
        return dailyDeviceRepo.insertIgnore(aggDate, contentId, deviceId) > 0;
    }

    @Override
    public void upsertDailyAgg(LocalDate aggDate, UUID contentId, int playInc, int uniqueDeviceInc, long watchTimeIncMs, int completeInc) {
        aggRepo.upsert(aggDate, contentId, playInc, uniqueDeviceInc, watchTimeIncMs, completeInc);
    }

    @Override
    public List<DailyRow> findDaily(UUID contentId, LocalDate from, LocalDate to) {
        return aggRepo.findDaily(contentId, from, to).stream()
                .map(r -> new DailyRow((LocalDate) r[0], ((Number) r[1]).intValue(), ((Number) r[2]).intValue(), ((Number) r[3]).longValue(), ((Number) r[4]).intValue()))
                .toList();
    }

    @Override
    public List<TopRow> findTop(LocalDate from, LocalDate to, int limit, String metric) {
        String m = (metric == null || metric.isBlank()) ? "watchTimeMs" : metric;
        return aggRepo.findTop(from, to, m, limit).stream()
                .map(r -> new TopRow((UUID) r[0], ((Number) r[1]).longValue(), ((Number) r[2]).longValue(), ((Number) r[3]).longValue(), ((Number) r[4]).longValue()))
                .toList();
    }
}
