alter table content
    add column if not exists status varchar(20) not null default 'DRAFT';

alter table content
    drop constraint ck_content_status;

alter table content
    add constraint ck_content_status
    check (status in ('DRAFT','PUBLISHED','ARCHIVED'));
