package com.aiott.ottpoc.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * 콘텐츠 접근 등급 조회 포트.
 * 구독 플랜 강제 적용에 사용됩니다.
 */
public interface ContentAccessPort {

    /**
     * 특정 콘텐츠에 접근하는 데 필요한 최소 구독 등급을 반환합니다.
     * 콘텐츠가 존재하지 않으면 빈 Optional을 반환합니다.
     */
    Optional<String> findRequiredTier(UUID contentId);
}
