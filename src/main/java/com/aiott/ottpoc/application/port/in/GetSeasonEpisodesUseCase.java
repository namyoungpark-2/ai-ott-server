package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.EpisodeResult;

import java.util.List;
import java.util.UUID;

public interface GetSeasonEpisodesUseCase {
    List<EpisodeResult> getSeasonEpisodes(UUID seasonId, String lang);
}
