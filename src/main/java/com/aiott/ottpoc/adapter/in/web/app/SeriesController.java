package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.adapter.in.web.LangResolver;
import com.aiott.ottpoc.application.dto.SeriesDetailResult;
import com.aiott.ottpoc.application.port.in.GetSeriesDetailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/app/series")
@RequiredArgsConstructor
public class SeriesController {

    private final GetSeriesDetailUseCase getSeriesDetailUseCase;

    @GetMapping("/{seriesId}")
    public SeriesDetailResult getSeriesDetail(
            @PathVariable UUID seriesId,
            @RequestParam(required = false) String lang
    ) {
        return getSeriesDetailUseCase.getSeriesDetail(seriesId, LangResolver.resolve(lang));
    }
}
