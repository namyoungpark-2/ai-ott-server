create table if not exists app_user (
    id              bigserial primary key,
    username        varchar(50)  not null unique,
    password_hash   varchar(255) not null,
    role            varchar(20)  not null default 'USER',
    subscription_tier varchar(20) not null default 'FREE',
    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now()
);
