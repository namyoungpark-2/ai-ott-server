package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.WatchEventIngestCommand;
import com.aiott.ottpoc.application.dto.WatchEventIngestResult;
import com.aiott.ottpoc.application.port.in.IngestWatchEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/watch-events")
public class WatchEventIngestController {

    private final IngestWatchEventUseCase ingestUseCase;

    @PostMapping
    public WatchEventIngestResult ingest(@RequestBody WatchEventIngestCommand cmd) {
        return ingestUseCase.ingest(cmd);
    }
}
