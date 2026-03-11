package com.aiott.ottpoc.adapter.in.web.app;

import com.aiott.ottpoc.application.dto.PlaybackResult;
import com.aiott.ottpoc.application.port.in.GetPlaybackUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/playback")
public class PlaybackController {

    private final GetPlaybackUseCase getPlaybackUseCase;

    @GetMapping("/{contentId}/playback")
    public PlaybackResult playback(@PathVariable UUID contentId) {
        return getPlaybackUseCase.getPlayback(contentId);
    }
}
