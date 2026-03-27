-- video_asset에 비디오 메타데이터 컬럼 추가 (하이브리드 플레이어 지원)
ALTER TABLE video_asset ADD COLUMN IF NOT EXISTS video_width   INTEGER NULL;
ALTER TABLE video_asset ADD COLUMN IF NOT EXISTS video_height  INTEGER NULL;
ALTER TABLE video_asset ADD COLUMN IF NOT EXISTS duration_ms   BIGINT  NULL;
