package com.aiott.ottpoc.application.dto;

import java.util.UUID;

public record FeedItemDto(
        UUID id,                 // contentId
        String title,
        String status,           // PROCESSING | READY | FAILED
        String thumbnailUrl,     // null 가능

        String contentType,      // MOVIE | EPISODE | CLIP | TRAILER | SHORT ...
        String kind,             // STANDALONE | SERIES

        UUID seriesId,           // EPISODE일 때만
        UUID seasonId,           // EPISODE일 때만
        Integer episodeNumber    // EPISODE일 때만
) {}
