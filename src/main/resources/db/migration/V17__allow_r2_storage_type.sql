-- StorageType enum 에는 LOCAL, S3, R2 가 있지만 V2 에서 만든 체크 제약은
-- ('LOCAL','S3') 만 허용한다. R2 가 나중에 enum 에 추가되면서 제약이 함께
-- 갱신되지 않았고, 그동안 로컬 스토리지 모드로만 구동해 드러나지 않았다.
--
-- STORAGE_TYPE=r2 로 켜면 업로드가 이 제약에 걸려 실패한다:
--   ERROR: new row for relation "video_asset" violates check constraint
--          "ck_video_asset_storage"
--
-- storage 컬럼은 varchar(10) 이라 'R2' 는 길이 제한에 걸리지 않는다.

alter table video_asset drop constraint if exists ck_video_asset_storage;
alter table video_asset add constraint ck_video_asset_storage
  check (storage in ('LOCAL', 'S3', 'R2'));

alter table image_asset drop constraint if exists ck_image_storage;
alter table image_asset add constraint ck_image_storage
  check (storage in ('LOCAL', 'S3', 'R2'));
