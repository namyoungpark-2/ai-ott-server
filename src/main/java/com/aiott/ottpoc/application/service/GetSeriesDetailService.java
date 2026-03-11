package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.SeriesDetailResult;
import com.aiott.ottpoc.application.dto.SeasonResult;
import com.aiott.ottpoc.application.port.in.GetSeriesDetailUseCase;
import com.aiott.ottpoc.application.port.out.SeriesQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSeriesDetailService implements GetSeriesDetailUseCase {

    private final SeriesQueryPort seriesQueryPort;

    @Override
    public SeriesDetailResult getSeriesDetail(UUID seriesId, String lang) {

        SeriesDetailResult.SeriesMeta meta =
                seriesQueryPort.loadSeriesMeta(seriesId, lang);

        List<SeasonResult> seasons =
                seriesQueryPort.loadSeasons(seriesId, lang);

        return new SeriesDetailResult(meta, seasons);
    }
}
