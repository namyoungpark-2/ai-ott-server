#!/usr/bin/env bash
# R2 백업에서 Postgres 를 복원한다.
#
#   ./restore.sh              최신 일별 백업으로 복원
#   ./restore.sh --list       사용 가능한 백업 목록만 출력
#   ./restore.sh aiott-20260805-030000.sql.gz
#
# ⚠️ 기존 데이터를 덮어쓴다(덤프가 --clean --if-exists 로 생성됨).
set -euo pipefail

cd "$(dirname "$0")"
set -a; source .env; set +a

: "${R2_ACCOUNT_ID:?}" "${R2_ACCESS_KEY_ID:?}" "${R2_SECRET_ACCESS_KEY:?}"
BUCKET="${BACKUP_BUCKET:-ai-ott-backups}"
ENDPOINT="https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com"

export AWS_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
export AWS_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
export AWS_DEFAULT_REGION=auto

list_backups() {
    for prefix in daily weekly; do
        echo "── ${prefix} ──"
        aws s3 ls "s3://${BUCKET}/${prefix}/" --endpoint-url "$ENDPOINT" \
            | awk '{printf "  %s %s  %10.2f MB\n", $1, $4, $3/1048576}'
    done
}

if [[ "${1:-}" == "--list" ]]; then
    list_backups
    exit 0
fi

target="${1:-}"
if [[ -z "$target" ]]; then
    target=$(aws s3 ls "s3://${BUCKET}/daily/" --endpoint-url "$ENDPOINT" \
        | awk '{print $4}' | sort | tail -1)
    [[ -z "$target" ]] && { echo "백업이 없습니다."; exit 1; }
    echo "최신 백업을 사용합니다: ${target}"
fi

# daily 에 없으면 weekly 에서 찾는다.
prefix=daily
aws s3 ls "s3://${BUCKET}/daily/${target}" --endpoint-url "$ENDPOINT" >/dev/null 2>&1 || prefix=weekly

tmp="/tmp/${target}"
aws s3 cp "s3://${BUCKET}/${prefix}/${target}" "$tmp" --endpoint-url "$ENDPOINT" --only-show-errors
echo "다운로드 완료: ${target} ($(du -h "$tmp" | cut -f1))"

read -r -p "현재 DB 를 이 백업으로 덮어씁니다. 계속하려면 'yes' 입력: " confirm
[[ "$confirm" != "yes" ]] && { echo "취소했습니다."; rm -f "$tmp"; exit 1; }

# 복원 중 애플리케이션이 쓰기를 하지 못하게 잠시 내린다.
echo "app 컨테이너 정지"
docker compose stop app

gunzip -c "$tmp" | docker compose exec -T postgres \
    psql -U "${POSTGRES_USER:-aiott}" -d "${POSTGRES_DB:-aiott}" -v ON_ERROR_STOP=1 --quiet

echo "app 컨테이너 재기동"
docker compose start app
rm -f "$tmp"

echo "복원 완료. 상태 확인:"
echo "  docker compose logs -f app"
echo "  curl -fsS https://api.aiott.kr/health"
