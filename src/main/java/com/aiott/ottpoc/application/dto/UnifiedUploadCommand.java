package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record UnifiedUploadCommand(
        UUID contentId,         // ✅ B 플로우: 기존 콘텐츠에 asset 붙일 때 사용(없으면 새로 생성)
        String mode,            // MOVIE | EPISODE
        String title,

        UUID seriesId,          // EPISODE일 때 선택(기존 시리즈)
        String seriesTitle,     // EPISODE일 때 선택(새 시리즈)
        Integer seasonNumber,   // EPISODE일 때 선택
        Integer episodeNumber   // EPISODE일 때 선택
) {}
