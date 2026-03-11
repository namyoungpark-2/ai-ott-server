package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.WatchEventIngestCommand;
import com.aiott.ottpoc.application.dto.WatchEventIngestResult;
import com.aiott.ottpoc.application.port.in.IngestWatchEventUseCase;
import com.aiott.ottpoc.application.port.out.WatchEventCommandPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class IngestWatchEventService implements IngestWatchEventUseCase {

    private final WatchEventCommandPort commandPort;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public WatchEventIngestResult ingest(WatchEventIngestCommand cmd) {
        // 최소 검증(POC)
        if (cmd.clientEventId() == null || cmd.clientEventId().isBlank()) {
            return new WatchEventIngestResult(false, false, null);
        }
        if (cmd.occurredAt() == null || cmd.contentId() == null) {
            return new WatchEventIngestResult(false, false, null);
        }
        if (cmd.deviceId() == null || cmd.deviceId().isBlank() || cmd.sessionId() == null || cmd.sessionId().isBlank()) {
            return new WatchEventIngestResult(false, false, null);
        }

        String extraJson = null;
        try {
            if (cmd.extra() != null) extraJson = objectMapper.writeValueAsString(cmd.extra());
        } catch (Exception ignore) {}

        var toStore = new WatchEventCommandPort.WatchEventToStore(
                cmd.clientEventId(),
                cmd.eventType(),
                cmd.occurredAt(),

                cmd.contentId(),
                cmd.videoAssetId(),

                cmd.sessionId(),
                cmd.deviceId(),

                cmd.positionMs(),
                cmd.deltaMs(),
                cmd.durationMs(),
                cmd.playbackRate(),

                cmd.country(),
                cmd.player(),
                cmd.appVersion(),
                cmd.networkType(),

                extraJson
        );

        var storedIdOpt = commandPort.storeRaw(toStore);
        if (storedIdOpt.isEmpty()) {
            // client_event_id 중복 -> 멱등 OK
            return new WatchEventIngestResult(true, true, null);
        }

        // 집계 업데이트(POC 1.5: 즉시 upsert)
        LocalDate aggDate = cmd.occurredAt().toLocalDate();

        int playInc = "PLAY".equalsIgnoreCase(cmd.eventType()) ? 1 : 0;
        int completeInc = "COMPLETE".equalsIgnoreCase(cmd.eventType()) ? 1 : 0;
        long watchInc = 0L;
        if ("PROGRESS".equalsIgnoreCase(cmd.eventType()) || "HEARTBEAT".equalsIgnoreCase(cmd.eventType())) {
            watchInc = cmd.deltaMs() == null ? 0L : Math.max(0, cmd.deltaMs());
        }

        int uniqueInc = commandPort.ensureDailyUniqueDevice(aggDate, cmd.contentId(), cmd.deviceId()) ? 1 : 0;

        commandPort.upsertDailyAgg(aggDate, cmd.contentId(), playInc, uniqueInc, watchInc, completeInc);

        return new WatchEventIngestResult(true, false, storedIdOpt.get());
    }
}
