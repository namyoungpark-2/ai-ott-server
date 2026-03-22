-- V10: Stripe 결제 연동 - 고객 ID, 구독 ID 추적
ALTER TABLE users
    ADD COLUMN stripe_customer_id  VARCHAR(64),
    ADD COLUMN stripe_subscription_id VARCHAR(64),
    ADD COLUMN subscription_expires_at TIMESTAMPTZ;

CREATE INDEX idx_users_stripe_customer_id
    ON users (stripe_customer_id)
    WHERE stripe_customer_id IS NOT NULL;

CREATE INDEX idx_users_stripe_subscription_id
    ON users (stripe_subscription_id)
    WHERE stripe_subscription_id IS NOT NULL;
