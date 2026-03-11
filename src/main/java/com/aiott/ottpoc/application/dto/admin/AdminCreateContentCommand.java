package com.aiott.ottpoc.application.dto.admin;

import java.util.UUID;

public record AdminCreateContentCommand(
        String mode,          // MOVIE | EPISODE
        String title,

        UUID seriesId,        // EPISODE 선택
        String seriesTitle,   // EPISODE 선택(새 시리즈)
        Integer seasonNumber, // EPISODE 선택
        Integer episodeNumber // EPISODE 선택
) {}
