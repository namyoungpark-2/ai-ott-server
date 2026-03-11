package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.SeriesDetailResult;
import com.aiott.ottpoc.application.dto.SeasonResult;

import java.util.List;
import java.util.UUID;

public interface SeriesQueryPort {

    SeriesDetailResult.SeriesMeta loadSeriesMeta(UUID seriesId, String lang);

    List<SeasonResult> loadSeasons(UUID seriesId, String lang);
}
