-- =============================================
-- V16: Creator & Channel System
-- =============================================

-- 1. channel table
CREATE TABLE channel (
    id              UUID PRIMARY KEY,
    owner_id        UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    handle          VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    profile_image_url VARCHAR(500),
    banner_image_url  VARCHAR(500),
    is_official     BOOLEAN      NOT NULL DEFAULT false,
    subscriber_count INT         NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 2. channel_i18n table
CREATE TABLE channel_i18n (
    id          BIGSERIAL PRIMARY KEY,
    channel_id  UUID        NOT NULL REFERENCES channel(id) ON DELETE CASCADE,
    lang        VARCHAR(10) NOT NULL,
    name        VARCHAR(100),
    description TEXT,
    UNIQUE(channel_id, lang)
);

-- 3. channel_subscription table
CREATE TABLE channel_subscription (
    subscriber_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_id    UUID        NOT NULL REFERENCES channel(id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (subscriber_id, channel_id)
);

-- 4. Add channel_id to content
ALTER TABLE content ADD COLUMN IF NOT EXISTS channel_id UUID REFERENCES channel(id) ON DELETE SET NULL;

-- 5. Add channel_id to series
ALTER TABLE series ADD COLUMN IF NOT EXISTS channel_id UUID REFERENCES channel(id) ON DELETE SET NULL;

-- 6. Insert official system channel
INSERT INTO channel (id, owner_id, handle, name, description, is_official)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    NULL,
    'official',
    'AI OTT Official',
    'Official AI OTT platform channel',
    true
) ON CONFLICT (handle) DO NOTHING;

-- 7. Update existing content and series to belong to official channel
UPDATE content SET channel_id = '00000000-0000-0000-0000-000000000001' WHERE channel_id IS NULL;
UPDATE series SET channel_id = '00000000-0000-0000-0000-000000000001' WHERE channel_id IS NULL;

-- 8. Add NOT NULL constraint to channel_id
ALTER TABLE content ALTER COLUMN channel_id SET NOT NULL;
ALTER TABLE series ALTER COLUMN channel_id SET NOT NULL;

-- 9. Indexes
CREATE INDEX idx_channel_owner ON channel(owner_id);
CREATE INDEX idx_channel_handle ON channel(handle);
CREATE INDEX idx_channel_subscription_channel ON channel_subscription(channel_id);
CREATE INDEX idx_content_channel ON content(channel_id);
CREATE INDEX idx_series_channel ON series(channel_id);
