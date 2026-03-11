package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record WatchEventIngestResult(
        boolean accepted,
        boolean duplicate,
        UUID storedEventId
) {}
