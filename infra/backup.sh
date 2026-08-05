#!/usr/bin/env bash
# Postgres 논리 백업을 Cloudflare R2 로 올린다.
# R2 는 S3 호환이라 aws CLI 를 --endpoint-url 로 그대로 쓴다.
#
# 크론: 0 3 * * *  (setup.sh 가 등록)
# 수동: ./backup.sh
set -euo pipefail

cd "$(dirname "$0")"
set -a; source .env; set +a

: "${R2_ACCOUNT_ID:?}" "${R2_ACCESS_KEY_ID:?}" "${R2_SECRET_ACCESS_KEY:?}"
BUCKET="${BACKUP_BUCKET:-ai-ott-backups}"
KEEP_DAILY="${BACKUP_KEEP_DAILY:-7}"
ENDPOINT="https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com"

export AWS_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
export AWS_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
export AWS_DEFAULT_REGION=auto

stamp=$(date +%Y%m%d-%H%M%S)
file="aiott-${stamp}.sql.gz"
tmp="/tmp/${file}"

echo "[$(date -u +%FT%TZ)] 백업 시작"

# 컨테이너 안에서 pg_dump 를 실행한다 — 호스트에 postgres 클라이언트가 없어도 된다.
docker compose exec -T postgres \
    pg_dump -U "${POSTGRES_USER:-aiott}" -d "${POSTGRES_DB:-aiott}" --clean --if-exists \
    | gzip -9 > "$tmp"

size=$(du -h "$tmp" | cut -f1)
[[ ! -s "$tmp" ]] && { echo "덤프가 비어 있습니다. 중단."; rm -f "$tmp"; exit 1; }

aws s3 cp "$tmp" "s3://${BUCKET}/daily/${file}" --endpoint-url "$ENDPOINT" --only-show-errors
echo "  업로드 완료: daily/${file} (${size})"

# 매주 월요일 판본은 weekly 로도 복사해 장기 보존한다.
if [[ $(date +%u) -eq 1 ]]; then
    aws s3 cp "s3://${BUCKET}/daily/${file}" "s3://${BUCKET}/weekly/${file}" \
        --endpoint-url "$ENDPOINT" --only-show-errors
    echo "  주간 사본 생성"
fi

rm -f "$tmp"

# 오래된 일별 백업 정리 — 최근 KEEP_DAILY 개만 남긴다.
#
# mapfile(bash 4+) 과 head -n -N(GNU 전용) 을 쓰지 않는다. 대상 VM 은 Ubuntu 라
# 둘 다 동작하지만, 그러면 스크립트를 다른 환경에서 검증할 수 없다. 회전은
# 실패해도 조용히 넘어가 백업이 계속 쌓이는 쪽이라 검증 가능해야 한다.
keys=$(aws s3 ls "s3://${BUCKET}/daily/" --endpoint-url "$ENDPOINT" \
    | awk '{print $4}' | grep -v '^$' | sort)
total=$(printf '%s\n' "$keys" | grep -c . || true)

if [ "${total:-0}" -gt "$KEEP_DAILY" ]; then
    printf '%s\n' "$keys" | head -n "$(( total - KEEP_DAILY ))" | while read -r key; do
        [ -z "$key" ] && continue
        aws s3 rm "s3://${BUCKET}/daily/${key}" --endpoint-url "$ENDPOINT" --only-show-errors
        echo "  회전 삭제: ${key}"
    done
else
    echo "  보관 ${total:-0}/${KEEP_DAILY} — 회전 없음"
fi

echo "[$(date -u +%FT%TZ)] 백업 완료"
