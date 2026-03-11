create index if not exists idx_transcoding_job_video_asset_created
  on transcoding_job (video_asset_id, created_at desc);

-- “현재 진행 중인 job이 있는지” 체크에 도움
create index if not exists idx_transcoding_job_video_asset_status
  on transcoding_job (video_asset_id, status);
