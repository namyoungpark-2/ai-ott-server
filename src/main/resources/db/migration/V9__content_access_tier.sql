-- V9: 콘텐츠별 접근 등급 - FREE/BASIC/PREMIUM 콘텐츠 구분
ALTER TABLE content
    ADD COLUMN required_tier VARCHAR(20) NOT NULL DEFAULT 'FREE';

-- required_tier 체크 제약 조건
ALTER TABLE content
    ADD CONSTRAINT ck_content_required_tier
        CHECK (required_tier IN ('FREE', 'BASIC', 'PREMIUM'));

-- 구독 플랜 기반 콘텐츠 필터링 인덱스
CREATE INDEX idx_content_required_tier ON content (required_tier);
