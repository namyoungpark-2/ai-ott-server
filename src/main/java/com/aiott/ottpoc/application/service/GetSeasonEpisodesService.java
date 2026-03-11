package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.EpisodeResult;
import com.aiott.ottpoc.application.port.in.GetSeasonEpisodesUseCase;
import com.aiott.ottpoc.application.port.out.SeasonQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSeasonEpisodesService implements GetSeasonEpisodesUseCase {

    private final SeasonQueryPort seasonQueryPort;

    @Override
    public List<EpisodeResult> getSeasonEpisodes(UUID seasonId, String lang) {
        return seasonQueryPort.loadEpisodes(seasonId, lang);
    }
}
