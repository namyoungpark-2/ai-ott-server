package com.aiott.ottpoc.application.dto.channel;

import java.util.UUID;

public record CreatorCreateContentCommand(
        String mode,          // MOVIE | EPISODE
        String title,
        UUID seriesId,        // EPISODE only
        String seriesTitle,   // new series
        Integer seasonNumber, // EPISODE only
        Integer episodeNumber // EPISODE only
) {}
