-- category 테이블에 IAB 계층 구조 지원 추가
ALTER TABLE category ADD COLUMN IF NOT EXISTS parent_id UUID REFERENCES category(id) ON DELETE SET NULL;
ALTER TABLE category ADD COLUMN IF NOT EXISTS iab_code  VARCHAR(20);
ALTER TABLE category ADD COLUMN IF NOT EXISTS tier      INT NOT NULL DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_category_parent ON category(parent_id);
CREATE INDEX IF NOT EXISTS idx_category_iab_code ON category(iab_code);

-- IAB Content Taxonomy 3.0 기반 Tier 1 카테고리 시드
INSERT INTO category (id, slug, label, description, sort_order, is_active, default_language, iab_code, tier)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'movies',              'Movies',              'Film content',                    1,  true, 'en', 'IAB-MOVIES', 1),
    ('10000000-0000-0000-0000-000000000002', 'television',          'Television',          'TV series and shows',             2,  true, 'en', 'IAB-TV', 1),
    ('10000000-0000-0000-0000-000000000003', 'entertainment',       'Entertainment',       'General entertainment',           3,  true, 'en', 'IAB1', 1),
    ('10000000-0000-0000-0000-000000000004', 'news-and-politics',   'News & Politics',     'News and political content',      4,  true, 'en', 'IAB12', 1),
    ('10000000-0000-0000-0000-000000000005', 'sports',              'Sports',              'Sports content',                  5,  true, 'en', 'IAB17', 1),
    ('10000000-0000-0000-0000-000000000006', 'education',           'Education',           'Educational programming',         6,  true, 'en', 'IAB5', 1),
    ('10000000-0000-0000-0000-000000000007', 'science',             'Science',             'Science and nature',              7,  true, 'en', 'IAB15', 1),
    ('10000000-0000-0000-0000-000000000008', 'technology',          'Technology',          'Technology and computing',        8,  true, 'en', 'IAB19', 1),
    ('10000000-0000-0000-0000-000000000009', 'food-and-drink',      'Food & Drink',        'Cooking and food content',        9,  true, 'en', 'IAB8', 1),
    ('10000000-0000-0000-0000-000000000010', 'travel',              'Travel',              'Travel shows and docs',           10, true, 'en', 'IAB20', 1),
    ('10000000-0000-0000-0000-000000000011', 'health-and-fitness',  'Health & Fitness',    'Health and wellness',             11, true, 'en', 'IAB7', 1),
    ('10000000-0000-0000-0000-000000000012', 'music',               'Music',               'Music and audio content',         12, true, 'en', 'IAB-MUSIC', 1),
    ('10000000-0000-0000-0000-000000000013', 'documentary',         'Documentary',         'Documentary films and series',    13, true, 'en', 'IAB-DOC', 1),
    ('10000000-0000-0000-0000-000000000014', 'kids-and-family',     'Kids & Family',       'Family-friendly content',         14, true, 'en', 'IAB6', 1),
    ('10000000-0000-0000-0000-000000000015', 'animation',           'Animation',           'Animated content',                15, true, 'en', 'IAB-ANIM', 1)
ON CONFLICT (slug) DO NOTHING;

-- i18n 시드 (en)
INSERT INTO category_i18n (category_id, lang, label, description)
SELECT id, 'en', label, description FROM category WHERE iab_code IS NOT NULL
ON CONFLICT (category_id, lang) DO NOTHING;

-- i18n 시드 (ko) — slug 기반 조회로 기존 ID와 호환
INSERT INTO category_i18n (category_id, lang, label, description)
SELECT id, 'ko', t.label, t.description
FROM category c
JOIN (VALUES
    ('movies',              '영화',         '영화 콘텐츠'),
    ('television',          'TV 프로그램',  'TV 시리즈 및 방송'),
    ('entertainment',       '엔터테인먼트', '종합 엔터테인먼트'),
    ('news-and-politics',   '뉴스/정치',    '뉴스 및 정치 콘텐츠'),
    ('sports',              '스포츠',       '스포츠 콘텐츠'),
    ('education',           '교육',         '교육 프로그램'),
    ('science',             '과학',         '과학과 자연'),
    ('technology',          '기술',         '기술 및 컴퓨팅'),
    ('food-and-drink',      '음식',         '요리 및 음식 콘텐츠'),
    ('travel',              '여행',         '여행 프로그램'),
    ('health-and-fitness',  '건강/피트니스','건강과 웰빙'),
    ('music',               '음악',         '음악 콘텐츠'),
    ('documentary',         '다큐멘터리',   '다큐멘터리 영화 및 시리즈'),
    ('kids-and-family',     '키즈/가족',    '가족 친화 콘텐츠'),
    ('animation',           '애니메이션',   '애니메이션 콘텐츠')
) AS t(slug, label, description) ON c.slug = t.slug
ON CONFLICT (category_id, lang) DO NOTHING;

-- genre 시드 (IAB 기반 공통 장르)
INSERT INTO genre (id, slug, label, description, sort_order, is_active, default_language)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'action',     'Action',     'Action and adventure',    1,  true, 'en'),
    ('20000000-0000-0000-0000-000000000002', 'comedy',     'Comedy',     'Comedy content',          2,  true, 'en'),
    ('20000000-0000-0000-0000-000000000003', 'drama',      'Drama',      'Dramatic content',        3,  true, 'en'),
    ('20000000-0000-0000-0000-000000000004', 'thriller',   'Thriller',   'Thriller and suspense',   4,  true, 'en'),
    ('20000000-0000-0000-0000-000000000005', 'horror',     'Horror',     'Horror content',          5,  true, 'en'),
    ('20000000-0000-0000-0000-000000000006', 'romance',    'Romance',    'Romantic content',        6,  true, 'en'),
    ('20000000-0000-0000-0000-000000000007', 'sci-fi',     'Sci-Fi',     'Science fiction',         7,  true, 'en'),
    ('20000000-0000-0000-0000-000000000008', 'fantasy',    'Fantasy',    'Fantasy content',         8,  true, 'en'),
    ('20000000-0000-0000-0000-000000000009', 'mystery',    'Mystery',    'Mystery and crime',       9,  true, 'en'),
    ('20000000-0000-0000-0000-000000000010', 'documentary','Documentary','Documentary style',       10, true, 'en'),
    ('20000000-0000-0000-0000-000000000011', 'reality',    'Reality',    'Reality and competition',  11, true, 'en'),
    ('20000000-0000-0000-0000-000000000012', 'historical', 'Historical', 'Historical and period',   12, true, 'en')
ON CONFLICT (slug) DO NOTHING;

-- genre i18n (en)
INSERT INTO genre_i18n (genre_id, lang, label, description)
SELECT id, 'en', label, description FROM genre WHERE id::text LIKE '20000000%'
ON CONFLICT (genre_id, lang) DO NOTHING;

-- genre i18n (ko)
INSERT INTO genre_i18n (genre_id, lang, label, description)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'ko', '액션',       '액션과 어드벤처'),
    ('20000000-0000-0000-0000-000000000002', 'ko', '코미디',     '코미디 콘텐츠'),
    ('20000000-0000-0000-0000-000000000003', 'ko', '드라마',     '드라마 콘텐츠'),
    ('20000000-0000-0000-0000-000000000004', 'ko', '스릴러',     '스릴러와 서스펜스'),
    ('20000000-0000-0000-0000-000000000005', 'ko', '공포',       '공포 콘텐츠'),
    ('20000000-0000-0000-0000-000000000006', 'ko', '로맨스',     '로맨스 콘텐츠'),
    ('20000000-0000-0000-0000-000000000007', 'ko', 'SF',         '공상 과학'),
    ('20000000-0000-0000-0000-000000000008', 'ko', '판타지',     '판타지 콘텐츠'),
    ('20000000-0000-0000-0000-000000000009', 'ko', '미스터리',   '미스터리와 범죄'),
    ('20000000-0000-0000-0000-000000000010', 'ko', '다큐멘터리', '다큐멘터리 스타일'),
    ('20000000-0000-0000-0000-000000000011', 'ko', '리얼리티',   '리얼리티와 경쟁'),
    ('20000000-0000-0000-0000-000000000012', 'ko', '사극',       '역사와 시대극')
ON CONFLICT (genre_id, lang) DO NOTHING;
