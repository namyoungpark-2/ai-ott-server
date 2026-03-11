-- V3__watch_event_analytics.sql
create table if not exists watch_event (
  id uuid primary key default gen_random_uuid(),

  client_event_id varchar(100) not null, -- 멱등키(UUID)
  event_type varchar(20) not null,        -- PLAY, PROGRESS, PAUSE, SEEK, STOP, COMPLETE, ERROR

  occurred_at timestamptz not null,
  received_at timestamptz not null default now(),

  content_id uuid not null references content(id) on delete cascade,
  video_asset_id uuid null references video_asset(id) on delete set null,

  session_id varchar(100) not null,
  device_id varchar(100) not null,

  position_ms int not null,
  delta_ms int null,
  duration_ms int null,
  playback_rate numeric null,

  country varchar(2) null,
  player varchar(20) null,
  app_version varchar(30) null,
  network_type varchar(20) null,

  extra_json jsonb null,

  constraint uq_watch_event_client_event unique (client_event_id),
  constraint ck_watch_event_type check (event_type in ('PLAY','PROGRESS','PAUSE','SEEK','STOP','COMPLETE','ERROR'))
);

create index if not exists idx_watch_event_content_time
  on watch_event (content_id, occurred_at);

create index if not exists idx_watch_event_device_time
  on watch_event (device_id, occurred_at);

create index if not exists idx_watch_event_session_time
  on watch_event (session_id, occurred_at);


-- 유니크 디바이스 집계를 빠르게 하기 위한 보조 테이블(POC 1.5 체감이 큼)
create table if not exists watch_daily_device (
  agg_date date not null,
  content_id uuid not null references content(id) on delete cascade,
  device_id varchar(100) not null,

  created_at timestamptz not null default now(),

  primary key (agg_date, content_id, device_id)
);

create index if not exists idx_watch_daily_device_content_date
  on watch_daily_device (content_id, agg_date);


-- 콘텐츠별 일 집계(대시보드/조회 API용)
create table if not exists watch_agg_content_daily (
  agg_date date not null,
  content_id uuid not null references content(id) on delete cascade,

  play_count int not null default 0,
  unique_devices int not null default 0,
  watch_time_ms bigint not null default 0,
  complete_count int not null default 0,

  updated_at timestamptz not null default now(),

  primary key (agg_date, content_id)
);

create index if not exists idx_watch_agg_content_daily_date
  on watch_agg_content_daily (agg_date);
