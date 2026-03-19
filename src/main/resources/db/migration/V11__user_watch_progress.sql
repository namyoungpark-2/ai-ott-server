-- User-level playback progress (supports "Continue Watching" feature)
CREATE TABLE user_watch_progress (
    user_id     UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id  UUID          NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    position_ms BIGINT        NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, content_id)
);

CREATE INDEX idx_uwp_user_updated ON user_watch_progress (user_id, updated_at DESC);
