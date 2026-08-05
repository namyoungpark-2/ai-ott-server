#!/usr/bin/env bash
# Oracle Ampere A1 (Ubuntu 22.04/24.04, arm64) 최초 셋업.
# 멱등이므로 여러 번 실행해도 안전하다.
#
#   ssh ubuntu@<VM-IP>
#   git clone https://github.com/namyoungpark-2/ai-ott-server.git
#   cd ai-ott-server/infra && ./setup.sh
set -euo pipefail

log() { printf '\n\033[1;32m==>\033[0m %s\n' "$*"; }
warn() { printf '\n\033[1;33m[!]\033[0m %s\n' "$*"; }

[[ $EUID -eq 0 ]] && { echo "root 로 실행하지 마세요. ubuntu 계정으로 실행하세요."; exit 1; }

log "1/6 패키지 갱신"
sudo apt-get update -qq
sudo apt-get install -y -qq ca-certificates curl gnupg awscli jq

log "2/6 Docker 설치"
if ! command -v docker >/dev/null; then
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
        | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io \
        docker-buildx-plugin docker-compose-plugin
    sudo usermod -aG docker "$USER"
    warn "docker 그룹 반영을 위해 재로그인이 필요합니다. 이 스크립트를 다시 실행하세요."
else
    echo "이미 설치됨: $(docker --version)"
fi
sudo systemctl enable --now docker

# Oracle Ubuntu 이미지는 iptables 기본 정책이 22 번 외 인바운드를 막는다.
# 콘솔의 보안 목록(Security List)만 열어도 여기서 막히므로 둘 다 열어야 한다.
# 이걸 놓치면 "보안 목록은 열었는데 접속이 안 되는" 상황이 된다.
log "3/6 방화벽에 80/443 개방"
for port in 80 443; do
    if ! sudo iptables -C INPUT -p tcp --dport "$port" -j ACCEPT 2>/dev/null; then
        sudo iptables -I INPUT 5 -p tcp --dport "$port" -j ACCEPT
        echo "  +${port}/tcp 허용"
    else
        echo "  ${port}/tcp 이미 허용됨"
    fi
done
sudo netfilter-persistent save >/dev/null 2>&1 \
    || { sudo apt-get install -y -qq iptables-persistent && sudo netfilter-persistent save; }

log "4/6 디렉터리 준비"
cd "$(dirname "$0")"
mkdir -p certs
chmod 700 certs

if [[ ! -f certs/origin.pem || ! -f certs/origin.key ]]; then
    warn "Cloudflare Origin CA 인증서가 없습니다."
    cat <<'EOF'
  CF 대시보드 → SSL/TLS → Origin Server → Create Certificate
    - 호스트: api.aiott.kr, *.aiott.kr
    - 유효기간: 15년
  발급된 두 값을 아래 경로에 저장하세요.
    infra/certs/origin.pem   (Origin Certificate)
    infra/certs/origin.key   (Private Key)
  그리고 CF SSL/TLS 모드를 "Full (strict)" 로 변경하세요.
EOF
fi

log "5/6 .env 확인"
if [[ ! -f .env ]]; then
    cp .env.example .env
    chmod 600 .env
    warn ".env 를 생성했습니다. 값을 채운 뒤 다시 실행하세요."
    echo "  랜덤 시크릿:  openssl rand -base64 48"
    exit 0
fi
chmod 600 .env

missing=()
for key in POSTGRES_PASSWORD JWT_SECRET ADMIN_USERNAME ADMIN_PASSWORD OPS_USERNAME OPS_PASSWORD; do
    value=$(grep -E "^${key}=" .env | cut -d= -f2- || true)
    [[ -z "$value" ]] && missing+=("$key")
done
if (( ${#missing[@]} )); then
    warn ".env 에 다음 필수 값이 비어 있습니다: ${missing[*]}"
    echo "  이 값들은 기본값이 없어 비어 있으면 컨테이너가 기동에 실패합니다."
    exit 1
fi

log "6/6 백업 크론 등록 (매일 03:00 KST)"
cron_line="0 3 * * * cd $(pwd) && ./backup.sh >> /var/log/ai-ott-backup.log 2>&1"
if ! crontab -l 2>/dev/null | grep -qF "backup.sh"; then
    (crontab -l 2>/dev/null; echo "$cron_line") | crontab -
    sudo touch /var/log/ai-ott-backup.log
    sudo chown "$USER" /var/log/ai-ott-backup.log
    echo "  등록 완료"
else
    echo "  이미 등록됨"
fi

log "셋업 완료. 다음 명령으로 기동하세요:"
echo "  docker compose up -d --build"
echo "  docker compose logs -f app"
