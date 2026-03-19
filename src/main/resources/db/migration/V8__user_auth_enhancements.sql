-- V8: 사용자 인증 강화 - 이메일 인증, 비밀번호 재설정, 구독 플랜
ALTER TABLE users
    ADD COLUMN email_verified               BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verification_token     VARCHAR(64),
    ADD COLUMN email_verification_expires_at TIMESTAMPTZ,
    ADD COLUMN password_reset_token         VARCHAR(64),
    ADD COLUMN password_reset_expires_at    TIMESTAMPTZ,
    ADD COLUMN subscription_tier            VARCHAR(20)  NOT NULL DEFAULT 'FREE';

-- 이메일 인증 토큰 조회 인덱스
CREATE INDEX idx_users_email_verification_token
    ON users (email_verification_token)
    WHERE email_verification_token IS NOT NULL;

-- 비밀번호 재설정 토큰 조회 인덱스
CREATE INDEX idx_users_password_reset_token
    ON users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;

-- 구독 플랜 필터링 인덱스
CREATE INDEX idx_users_subscription_tier ON users (subscription_tier);
