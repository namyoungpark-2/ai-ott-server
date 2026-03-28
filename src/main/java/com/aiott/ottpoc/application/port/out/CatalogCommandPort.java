package com.aiott.ottpoc.application.port.out;

import java.util.UUID;

public interface CatalogCommandPort {

    UUID createMovieContent(String title);
    UUID createMovieContentWithChannel(String title, UUID channelId);

    // EPISODE 업로드용
    UUID createSeries(String title, String defaultLang);
    UUID createSeriesWithChannel(String title, String defaultLang, UUID channelId);
    UUID ensureSeason(UUID seriesId, int seasonNumber, String defaultLang);
    UUID createEpisodeContent(UUID seriesId, UUID seasonId, int episodeNumber, String title);

    int nextEpisodeNumber(UUID seasonId);
    void updateContentStatus(UUID contentId, String status);
}

