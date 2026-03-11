package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.PlaybackResult;

import java.util.UUID;

public interface GetPlaybackUseCase {
    PlaybackResult getPlayback(UUID contentId);
}
