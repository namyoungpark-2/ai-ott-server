alter table content add column if not exists slug varchar(255);
alter table content add column if not exists poster_url text;
alter table content add column if not exists banner_url text;
alter table content add column if not exists age_rating varchar(20);
alter table content add column if not exists is_featured boolean not null default false;

create unique index if not exists uq_content_slug on content(slug) where slug is not null;
create index if not exists idx_content_featured_release on content(is_featured, release_at desc);

create table if not exists category (
  id uuid primary key default gen_random_uuid(),
  slug varchar(120) not null unique,
  label varchar(120) not null,
  description text null,
  sort_order int not null default 0,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists content_category (
  content_id uuid not null references content(id) on delete cascade,
  category_id uuid not null references category(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (content_id, category_id)
);
create index if not exists idx_content_category_category on content_category(category_id, content_id);

create table if not exists tag (
  id uuid primary key default gen_random_uuid(),
  slug varchar(120) not null unique,
  label varchar(120) not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists content_tag (
  content_id uuid not null references content(id) on delete cascade,
  tag_id uuid not null references tag(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (content_id, tag_id)
);
create index if not exists idx_content_tag_tag on content_tag(tag_id, content_id);

insert into category (slug, label, description, sort_order)
values
  ('movie', 'Movies', 'Feature films and stand-alone videos', 10),
  ('series', 'Series', 'Serialized episodic programs', 20),
  ('kids', 'Kids', 'Family-friendly and kids titles', 30),
  ('sports', 'Sports', 'Sports live and VOD', 40),
  ('documentary', 'Documentary', 'Documentary and factual programming', 50)
on conflict (slug) do nothing;
