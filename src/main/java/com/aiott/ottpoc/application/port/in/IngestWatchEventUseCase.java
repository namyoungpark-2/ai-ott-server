package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.WatchEventIngestCommand;
import com.aiott.ottpoc.application.dto.WatchEventIngestResult;

public interface IngestWatchEventUseCase {
    WatchEventIngestResult ingest(WatchEventIngestCommand cmd);
}
