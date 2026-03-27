-- genre 테이블 (장르: Thriller, Comedy, Romance, Action 등)
CREATE TABLE IF NOT EXISTS genre (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(120) NOT NULL UNIQUE,
    label           VARCHAR(120) NOT NULL,
    description     TEXT,
    sort_order      INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    default_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- genre i18n
CREATE TABLE IF NOT EXISTS genre_i18n (
    id          BIGSERIAL PRIMARY KEY,
    genre_id    UUID         NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    lang        VARCHAR(10)  NOT NULL,
    label       VARCHAR(120) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (genre_id, lang)
);

-- content ↔ genre (N:N)
CREATE TABLE IF NOT EXISTS content_genre (
    content_id  UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    genre_id    UUID NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (content_id, genre_id)
);
CREATE INDEX IF NOT EXISTS idx_content_genre_genre ON content_genre(genre_id, content_id);
