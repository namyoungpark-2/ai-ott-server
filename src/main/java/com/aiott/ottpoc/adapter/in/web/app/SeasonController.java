package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.EpisodeResult;
import com.aiott.ottpoc.application.port.in.GetSeasonEpisodesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/app/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final GetSeasonEpisodesUseCase getSeasonEpisodesUseCase;

    @GetMapping("/{seasonId}/episodes")
    public List<EpisodeResult> getEpisodes(
            @PathVariable UUID seasonId,
            @RequestParam(defaultValue = "en") String lang
    ) {
        return getSeasonEpisodesUseCase.getSeasonEpisodes(seasonId, lang);
    }
}
