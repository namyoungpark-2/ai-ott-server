package com.aiott.ottpoc.application.port.out;

import com.aiott.ottpoc.application.dto.EpisodeResult;

import java.util.List;
import java.util.UUID;

public interface SeasonQueryPort {
    List<EpisodeResult> loadEpisodes(UUID seasonId, String lang);
}
