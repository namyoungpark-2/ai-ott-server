package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.SeriesDetailResult;

import java.util.UUID;

public interface GetSeriesDetailUseCase {
    SeriesDetailResult getSeriesDetail(UUID seriesId, String lang);
}
