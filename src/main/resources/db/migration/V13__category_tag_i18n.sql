-- category i18n
CREATE TABLE IF NOT EXISTS category_i18n (
    id          BIGSERIAL PRIMARY KEY,
    category_id UUID         NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    lang        VARCHAR(10)  NOT NULL,
    label       VARCHAR(120) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (category_id, lang)
);

-- tag i18n
CREATE TABLE IF NOT EXISTS tag_i18n (
    id          BIGSERIAL PRIMARY KEY,
    tag_id      UUID         NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    lang        VARCHAR(10)  NOT NULL,
    label       VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (tag_id, lang)
);

-- 기존 category 데이터를 en으로 마이그레이션
INSERT INTO category_i18n (category_id, lang, label, description)
SELECT id, 'en', label, description FROM category
ON CONFLICT (category_id, lang) DO NOTHING;

-- 기존 tag 데이터를 en으로 마이그레이션
INSERT INTO tag_i18n (tag_id, lang, label)
SELECT id, 'en', label FROM tag
ON CONFLICT (tag_id, lang) DO NOTHING;

-- category에 default_language 컬럼 추가
ALTER TABLE category ADD COLUMN IF NOT EXISTS default_language VARCHAR(10) NOT NULL DEFAULT 'en';

-- tag에 default_language 컬럼 추가
ALTER TABLE tag ADD COLUMN IF NOT EXISTS default_language VARCHAR(10) NOT NULL DEFAULT 'en';
