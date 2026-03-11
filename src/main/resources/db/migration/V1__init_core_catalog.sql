-- UUID 생성 확장 (Postgres)
create extension if not exists pgcrypto;

-- 1) series
create table if not exists series (
  id uuid primary key default gen_random_uuid(),
  status varchar(20) not null,
  default_language varchar(10) not null default 'en',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint ck_series_status check (status in ('DRAFT','PUBLISHED','UNLISTED','ARCHIVED'))
);

create table if not exists series_i18n (
  id bigserial primary key,
  series_id uuid not null references series(id) on delete cascade,
  lang varchar(10) not null,
  title varchar(255) not null,
  description text null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_series_i18n unique (series_id, lang)
);

create index if not exists idx_series_status_updated
  on series (status, updated_at desc);

-- 2) season
create table if not exists season (
  id uuid primary key default gen_random_uuid(),
  series_id uuid not null references series(id) on delete cascade,
  season_number int not null,
  status varchar(20) not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint ck_season_status check (status in ('DRAFT','PUBLISHED','UNLISTED','ARCHIVED')),
  constraint uq_season_series_number unique (series_id, season_number)
);

create table if not exists season_i18n (
  id bigserial primary key,
  season_id uuid not null references season(id) on delete cascade,
  lang varchar(10) not null,
  title varchar(255) not null,
  description text null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_season_i18n unique (season_id, lang)
);

create index if not exists idx_season_series_number
  on season (series_id, season_number);

-- 3) content (재생 단위)
create table if not exists content (
  id uuid primary key default gen_random_uuid(),

  content_type varchar(20) not null,
  status varchar(20) not null,

  series_id uuid null references series(id) on delete set null,
  season_id uuid null references season(id) on delete set null,

  episode_number int null,
  runtime_seconds int null,
  release_at timestamptz null,
  default_language varchar(10) not null default 'en',

  source_content_id uuid null references content(id) on delete set null,
  source_relation varchar(30) null,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),

  constraint ck_content_type check (
    content_type in ('MOVIE','EPISODE','LIVE','CLIP','TRAILER','SHORT','EXTRA')
  ),
  constraint ck_content_status check (
    status in ('DRAFT','PUBLISHED','UNLISTED','ARCHIVED')
  ),

  -- EPISODE일 때 필수값 강제
  constraint ck_episode_required_fields check (
    (content_type <> 'EPISODE')
    OR (series_id is not null and season_id is not null and episode_number is not null)
  ),

  -- EPISODE가 아닐 때는 episode_number가 없어야 한다(운영 깔끔)
  constraint ck_non_episode_fields check (
    (content_type = 'EPISODE') OR (episode_number is null)
  ),

  -- 파생 관계는 둘 중 하나만 들어가면 안됨(짝 맞춰)
  constraint ck_source_pair check (
    (source_content_id is null and source_relation is null)
    OR (source_content_id is not null and source_relation is not null)
  )
);

-- EPISODE: (season_id, episode_number) 유니크 (부분 유니크 인덱스)
create unique index if not exists uq_episode_per_season
  on content (season_id, episode_number)
  where content_type = 'EPISODE';

-- 파생 콘텐츠 조회 빠르게
create index if not exists idx_content_source
  on content (source_content_id, source_relation);

-- 피드/최신 정렬
create index if not exists idx_content_type_status_release
  on content (content_type, status, release_at desc);

-- 시리즈/시즌 조회(에피소드 리스트 등)
create index if not exists idx_content_series_season_ep
  on content (series_id, season_id, episode_number);

create table if not exists content_i18n (
  id bigserial primary key,
  content_id uuid not null references content(id) on delete cascade,
  lang varchar(10) not null,
  title varchar(255) not null,
  description text null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_content_i18n unique (content_id, lang)
);
