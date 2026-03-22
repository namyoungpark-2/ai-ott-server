package com.aiott.ottpoc.application.service;

import com.aiott.ottpoc.application.dto.PlaybackResult;
import com.aiott.ottpoc.application.port.in.GetPlaybackUseCase;
import com.aiott.ottpoc.application.port.out.ContentAccessPort;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import com.aiott.ottpoc.application.port.out.VideoAssetCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlaybackService implements GetPlaybackUseCase {

    private static final Map<String, Integer> TIER_RANK = Map.of(
            "FREE",    0,
            "BASIC",   1,
            "PREMIUM", 2
    );

    private final VideoAssetCommandPort videoAssetCommandPort;
    private final ContentAccessPort contentAccessPort;
    private final MediaStoragePort mediaStoragePort;

    @Override
    public PlaybackResult getPlayback(UUID contentId) {
        // 1) 콘텐츠에 READY 상태의 비디오 에셋이 있는지 확인
        var asset = videoAssetCommandPort.findReadyAssetByContentId(contentId)
                .orElseThrow(() -> new IllegalStateException(
                        "No READY video asset for contentId=" + contentId));

        // 2) 콘텐츠의 필요 구독 등급 확인
        String requiredTier = contentAccessPort.findRequiredTier(contentId).orElse("FREE");

        // 3) 현재 인증된 사용자의 구독 등급 확인
        String userTier = extractSubscriptionTier();

        // 4) 등급 비교: 사용자 등급 < 콘텐츠 요구 등급이면 거부
        if (tierRank(userTier) < tierRank(requiredTier)) {
            throw new AccessDeniedException(
                    "이 콘텐츠는 " + requiredTier + " 이상 구독이 필요합니다. 현재 플랜: " + userTier);
        }

        // 5) 스토리지 어댑터를 통해 재생 URL 생성 (R2이면 presigned URL, 로컬이면 원본 경로)
        String playbackUrl = mediaStoragePort.getPlaybackUrl(asset.hlsMasterKey());
        return new PlaybackResult(playbackUrl, asset.status());
    }

    private String extractSubscriptionTier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object tier = jwtAuth.getTokenAttributes().get("subscription_tier");
            if (tier instanceof String s) return s;
        }
        return "FREE";
    }

    private int tierRank(String tier) {
        return TIER_RANK.getOrDefault(tier != null ? tier.toUpperCase() : "FREE", 0);
    }
}
