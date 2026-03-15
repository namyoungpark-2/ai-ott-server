alter table content
    add column if not exists status varchar(20) not null default 'DRAFT';

-- V1 already created this constraint; drop first so we can redefine cleanly
alter table content
    drop constraint if exists ck_content_status;

alter table content
    add constraint ck_content_status
    check (status in ('DRAFT','PUBLISHED','ARCHIVED'));
