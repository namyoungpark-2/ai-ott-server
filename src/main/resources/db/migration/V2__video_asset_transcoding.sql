create table if not exists video_asset (
  id uuid primary key default gen_random_uuid(),
  content_id uuid not null references content(id) on delete cascade,

  storage varchar(10) not null, -- LOCAL, S3
  source_key varchar(500) not null, -- 원본 파일 경로/키
  hls_master_key varchar(500) null, -- master.m3u8 경로/키

  status varchar(20) not null, -- UPLOADED, TRANSCODING, READY, FAILED
  error_message text null,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),

  constraint ck_video_asset_storage check (storage in ('LOCAL','S3')),
  constraint ck_video_asset_status check (status in ('UPLOADED','TRANSCODING','READY','FAILED'))
);

create index if not exists idx_video_asset_content_status
  on video_asset (content_id, status);

create table if not exists transcoding_job (
  id uuid primary key default gen_random_uuid(),
  video_asset_id uuid not null references video_asset(id) on delete cascade,

  status varchar(20) not null, -- QUEUED, RUNNING, SUCCEEDED, FAILED
  error_message text null,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),

  constraint ck_transcoding_job_status check (status in ('QUEUED','RUNNING','SUCCEEDED','FAILED'))
);

create index if not exists idx_transcoding_job_asset
  on transcoding_job (video_asset_id, status);

create table if not exists image_asset (
  id uuid primary key default gen_random_uuid(),
  content_id uuid not null references content(id) on delete cascade,

  kind varchar(20) not null, -- THUMBNAIL, POSTER, BACKDROP
  storage varchar(10) not null,
  storage_key varchar(500) not null,

  width int null,
  height int null,

  created_at timestamptz not null default now(),

  constraint ck_image_kind check (kind in ('THUMBNAIL','POSTER','BACKDROP')),
  constraint ck_image_storage check (storage in ('LOCAL','S3'))
);

create index if not exists idx_image_asset_content_kind
  on image_asset (content_id, kind);
