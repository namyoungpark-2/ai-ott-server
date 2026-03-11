package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.PlaybackResult;
import com.aiott.ottpoc.application.port.in.GetPlaybackUseCase;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlaybackService implements GetPlaybackUseCase {

    private final VideoAssetCommandPort videoAssetCommandPort;

    @Override
    public PlaybackResult getPlayback(UUID contentId) {
        var asset = videoAssetCommandPort.findReadyAssetByContentId(contentId)
                .orElseThrow(() -> new IllegalStateException("No READY video asset for contentId=" + contentId));

        // 지금은 로컬 파일 경로를 그대로 반환 (다음 단계에서 signed URL로 진화)
        return new PlaybackResult(asset.hlsMasterKey(), asset.status());
    }
}
