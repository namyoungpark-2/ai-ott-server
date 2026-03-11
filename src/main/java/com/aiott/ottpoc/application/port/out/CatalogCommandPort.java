package com.aiott.ottpoc.application.port.out;

import java.util.UUID;

public interface CatalogCommandPort {

    UUID createMovieContent(String title);

    // EPISODE 업로드용
    UUID createSeries(String title, String defaultLang);
    UUID ensureSeason(UUID seriesId, int seasonNumber, String defaultLang);
    UUID createEpisodeContent(UUID seriesId, UUID seasonId, int episodeNumber, String title);

    int nextEpisodeNumber(UUID seasonId);
    void updateContentStatus(UUID contentId, String status);
}

