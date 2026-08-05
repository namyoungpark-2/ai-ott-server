# 운영 배포 구성 — ngrok 로컬 백엔드에서 상시 가동 서비스로

> 작성: 2026-08-05
> 범위: `ai-ott-server`, `ai-ott-web`, `ai-ott-admin` (앱은 빌드 플래그만)
> 목표 비용: **월 $0**

---

## 1. 왜 (Why)

### 문제

`aiott.kr` 은 실제로 서비스되고 있었지만, **백엔드가 개발자 노트북의 ngrok 터널**이었다.

```
프로덕션 API 주소 = https://18aa8e56e5bc.ngrok.app
현재 상태        = HTTP 404, ngrok-error-code: ERR_NGROK_3200 ("endpoint is offline")
```

커밋 `8a4ab3e fix: set ngrok backend URL as default for production/staging environments`
가 이 주소를 운영 기본값으로 박아 넣었다. 결과:

| 증상 | 원인 |
|---|---|
| 노트북을 끄면 서비스 중단 | 백엔드가 로컬 머신 |
| 터널이 끊긴 뒤에도 사이트는 HTTP 200 | 프론트는 CF Workers 에 있고 API 만 죽음 → 원인 파악이 어려움 |
| 환경변수를 안 넣어도 빌드 성공 | ngrok 주소가 코드의 fallback |
| 저장소만 봐서는 배포처를 알 수 없음 | 도메인·워커 연결이 CF 대시보드에만 존재 |

### 배포 준비 중 발견한 보안 결함

인프라만 옮기려 했으나, 공개 배포를 막아야 하는 결함이 함께 발견됐다.

| # | 결함 | 영향 |
|---|---|---|
| 1 | `SecurityConfig` 가 `anyRequest().permitAll()` | `/api/admin/**` 13개 컨트롤러 **전부 무인증** |
| 2 | `@EnableMethodSecurity` 부재 | `@PreAuthorize` 가 있어도 무시됨 (게다가 실제 적용된 메서드는 0개) |
| 3 | `JWT_SECRET` 기본값이 공개 문자열 | 누구나 `aud=admin`, `ROLE_ADMIN` 토큰 **위조 가능** |
| 4 | `ADMIN_PASSWORD` 기본값 `admin` | 환경변수 없이 배포하면 admin/admin 으로 열림 |
| 5 | `JwtAuthConverter` 미배선 (죽은 코드) | 토큰 `aud` 검증 미작동 → 체인 간 교차 사용 가능 |
| 6 | `RateLimitingFilter` 가 `X-Forwarded-For` 우선 | 클라이언트가 헤더를 위조해 **레이트리밋 무력화** |
| 7 | `lazy-initialization: true` | 필수 설정 누락을 기동 시점에 못 잡음 |

3번이 가장 심각하다. 1번(무인증)은 나중에 인증을 붙여도, 3번이 남아 있으면 공격자가
유효한 관리자 토큰을 스스로 만들 수 있다.

`AdminUserController` 에는 `// All routes under /api/admin/** are secured to ROLE_ADMIN
by SecurityConfig` 라는 주석이 있었다. **의도는 있었고 배선만 되지 않은 상태**였다.
로컬에서만 구동했으므로 드러날 기회가 없었다.

### R2 전환 시 업로드가 실패하는 문제

`StorageType` enum 은 `LOCAL, S3, R2` 인데, V2 마이그레이션이 만든 체크 제약은
`('LOCAL','S3')` 만 허용한다. R2 가 나중에 enum 에 추가되면서 제약이 함께 갱신되지
않았고, 지금까지 로컬 모드로만 구동해 드러나지 않았다.

```
ERROR: new row for relation "video_asset" violates check constraint
       "ck_video_asset_storage"
```

`video_asset` 과 `image_asset` 두 테이블에 같은 문제가 있다.
**V17__allow_r2_storage_type.sql** 로 두 제약에 `'R2'` 를 추가했다.

### R2 전환 시 재생이 깨지는 문제

`STORAGE_TYPE=r2` 로 켜기 전에 확인한 결과, **그대로는 재생이 동작하지 않는다.**

```
1. ffmpeg 가 만든 master.m3u8 의 세그먼트 경로는 상대 경로다 → seg_000.ts
2. R2MediaStorageAdapter#getPlaybackUrl 은 마스터를 presigned URL 로 바꾼다
   → https://<account>.r2.cloudflarestorage.com/<bucket>/hls/<id>/master.m3u8?X-Amz-...
3. 플레이어는 seg_000.ts 를 그 S3 엔드포인트 기준으로 요청한다 (서명 없음)
4. R2 의 S3 API 는 항상 SigV4 를 요구한다 → 모든 세그먼트 403 → 재생 불가
```

마스터 하나만 서명해도 세그먼트는 보호되지 않는다는 점은 코드 주석에도 적혀 있었지만,
그것이 **기능 자체를 깨뜨린다**는 점은 반영돼 있지 않았다. 로컬 스토리지 모드에서만
동작을 확인해 왔기 때문이다.

조치: `app.r2.presign-playback` 플래그를 추가하고 **기본값을 false** 로 둔다.
false 면 저장된 공개 URL(`cdn.aiott.kr/hls/.../master.m3u8`)을 그대로 내려주므로
세그먼트도 같은 공개 도메인에서 정상 로드된다. presign 코드는 남겨 두었고,
세그먼트까지 함께 보호하는 방식(플레이리스트 재작성 또는 Cloudflare Signed Token)을
구현할 때 다시 켜면 된다.

대가는 **`cdn.aiott.kr` URL 을 아는 사람은 로그인 없이 시청 가능**하다는 것이다.
지인 베타에서는 감수하고, 유료화 시점에 해결한다.

### 왜 Oracle Always Free 인가

비용 0 이 제약이었고, 영상 트랜스코딩이 관건이었다.

- `Dockerfile.render` 는 `-Xmx400m` / 512MB 컨테이너를 전제했다. FFmpeg 가 API 와
  같은 컨테이너에서 도는 구조(`@Async`)라 **장분 영상 한 건에 서비스 전체가 흔들린다.**
- 무료 PaaS(Render/Fly 등)는 이 문제를 돈으로만 해결할 수 있다.
- Oracle Ampere A1 은 **4코어 / 24GB RAM 을 영구 무료**로 준다. 인프라를 바꾸는 것만으로
  코드 수정 없이 트랜스코딩 여유가 확보된다.
- Cloudflare Stream(매니지드)은 ABR·서명 URL을 공짜로 얻지만 **최소 월 $5 선**이라
  비용 0 제약에서 탈락했다.

---

## 2. AS-IS → TO-BE

### AS-IS

```
브라우저
  ├─ aiott.kr ─────────────► CF Workers (ai-ott-web)          [대시보드 수동 배포]
  └─ ai-ott-admin.jarneg23.workers.dev ► CF Workers (admin)    [배포 설정 미커밋]
                    │
                    ▼
        18aa8e56e5bc.ngrok.app  ✕ OFFLINE
                    │
              개발자 노트북
              ├─ Spring Boot (로컬)
              ├─ Postgres (로컬)
              ├─ FFmpeg (같은 JVM)
              └─ data/ (로컬 디스크)
```

### TO-BE

```
브라우저 / 앱
  ├─ https://aiott.kr ────────► CF Workers (ai-ott-web)     [무료]
  ├─ https://admin.aiott.kr ──► CF Workers (ai-ott-admin)   [무료]
  └─ https://cdn.aiott.kr ────► Cloudflare R2               [10GB·egress 무료]
                                  HLS 세그먼트 · 썸네일
        │
        │ 프론트의 /api/* 서버사이드 프록시
        ▼
  https://api.aiott.kr
        │ CF 프록시 (무료 SSL · DDoS · 오리진 IP 은닉)
        ▼
  ┌─────────────────────────────────────────┐
  │ Oracle Ampere A1 · 춘천 · ARM 4코어/24GB  │  영구 무료
  │  docker compose                          │
  │   ├─ caddy       TLS 종단 (CF Origin CA) │
  │   ├─ app         Spring Boot + FFmpeg    │
  │   └─ postgres    영구 볼륨               │
  └─────────────────────────────────────────┘
        │
        └─ 매일 03:00 pg_dump → R2 (백업)
```

### 비교표

| 항목 | AS-IS | TO-BE |
|---|---|---|
| 백엔드 위치 | 개발자 노트북 + ngrok | Oracle VM (춘천), 상시 가동 |
| 백엔드 주소 | 매번 바뀌는 ngrok URL | `api.aiott.kr` 고정 |
| DB | 로컬 Postgres | VM 내 컨테이너 + 일일 백업 |
| 영상 저장 | 로컬 디스크 | R2 + `cdn.aiott.kr` (egress 무료) |
| 영상 트래픽 | 노트북 대역폭 | VM 미경유 (R2 직접) |
| JVM 힙 / RAM | 400MB / 512MB 전제 | 2GB / 24GB |
| 어드민 인증 | **없음** | aud=admin + ROLE_ADMIN |
| JWT 시크릿 | 공개 기본값 | 필수 주입, 미설정 시 기동 실패 |
| 배포 방식 | 수동 (대시보드) | GitHub Actions |
| CI 배포 대상 | Vercel 워크플로 (미사용) | 제거 — 실제 경로인 CF Workers Builds 로 일원화 |
| 도메인 연결 근거 | 대시보드에만 존재 | `wrangler.jsonc` 에 명시 |
| 월 비용 | $0 (단 노트북 상시 가동) | **$0** |

---

## 3. 상태별 전체 흐름

### 업로드 → 재생 (크리에이터)

```
1. 브라우저 → POST api.aiott.kr/api/app/creator/contents/{id}/upload   [인증 필요]
2. app: TEMP_DIR 에 저장, transcoding_job 생성 → 즉시 202 반환
3. app @Async 스레드:
     ffmpeg  → HLS 세그먼트 생성      (24GB RAM 이라 장분 영상도 여유)
     ffprobe → 길이·해상도 메타데이터
     썸네일 추출
4. R2 업로드 → cdn.aiott.kr URL 을 video_asset 에 기록
5. transcoding_job = DONE, TEMP_DIR 정리
   ↑ 브라우저는 useContentPolling 으로 상태 폴링 (기존 구현)
```

### 시청 (구독 검사 포함)

```
1. 브라우저 → aiott.kr/watch/{id}        middleware 가 auth_token 쿠키 확인
2. Worker → api.aiott.kr/api/app/playback/{id}   [인증 필요]
3. app: 콘텐츠 접근 티어 vs 사용자 구독 등급 비교
     불충분 → 403
     충분   → cdn.aiott.kr HLS 마스터 URL 반환
4. 브라우저 → cdn.aiott.kr 에서 세그먼트 직접 수신  ← VM 미경유
```

이 4단계가 핵심이다. **영상 트래픽이 VM 을 통과하지 않으므로** 무료 VM 한 대로
수십 명 동시 시청을 견딘다.

### 인증 (경로별 체인 3개)

```
POST /auth/login          → aud=app,   ROLE_USER   → /api/app/**
POST /auth/admin/login    → aud=admin, ROLE_ADMIN  → /api/admin/**
POST /auth/ops/login      → aud=ops,   ROLE_SRE    → /api/ops/**
   ↑ 세 로그인 모두 IP·액션별 레이트리밋 (분당 5회)

@Order(1) /api/admin/**  aud=admin 검증 + hasRole('ADMIN')
@Order(2) /api/ops/**    aud=ops   검증 + hasRole('SRE')
@Order(3) 그 외          aud=app   검증
            공개: / /health /actuator/health /auth/** /api/webhooks/**
                  /hls/** /thumbnails/** /stream/**
                  GET 카탈로그·콘텐츠·시리즈·시즌·채널·피드
            인증: /api/app/{me,creator,payments,playback,watch-events,analytics}/**
                  /api/app/channels/*/{subscribe,subscription-status}
```

**규칙 순서가 중요하다.** 인증 필수 항목이 공개 GET 규칙보다 먼저 선언돼야
`/api/app/channels/{handle}/subscription-status` 같은 개인화 엔드포인트가 열리지 않는다.

### 장애 복구 (VM 소실)

```
1. 새 Ampere A1 인스턴스 생성 (춘천 → 막히면 서울)
2. git clone && cd infra && ./setup.sh
3. 보관해 둔 .env 복원 + CF Origin CA 인증서를 certs/ 에 배치
4. docker compose up -d --build
5. ./restore.sh                       # R2 최신 백업에서 DB 복원
6. CF DNS 의 api A 레코드를 새 IP 로 변경
   목표: 30분 이내
```

---

## 4. 파일 매핑

### ai-ott-server

| 파일 | 구분 | 내용 |
|---|---|---|
| `config/security/SecurityConfig.java` | 수정 | 단일 permitAll 체인 → 경로별 3체인, `@EnableMethodSecurity` |
| `config/security/JwtAuthConverter.java` | 수정 | `IllegalArgumentException`(500) → `InvalidBearerTokenException`(401) |
| `config/security/RateLimitingFilter.java` | 수정 | 두 인증 경로 정규화, admin/ops 로그인 포함, CF-Connecting-IP 우선 |
| `adapter/in/web/auth/AuthController.java` | 수정 | ADMIN/OPS 기본 자격증명 제거, 중복 Set-Cookie 제거 |
| `adapter/in/web/{admin,ops}/*Controller.java` (13) | 수정 | 클래스 레벨 `@PreAuthorize` 추가 |
| `config/FlywayMigrationChecker.java` | 수정 | `@Profile("!test")` — Boot 자동 마이그레이션과 중복 |
| `resources/application.yml` | 수정 | `JWT_SECRET` 기본값 제거, actuator 는 health 만, lazy-init off |
| `resources/db/migration/V17__allow_r2_storage_type.sql` | **신규** | 체크 제약에 `'R2'` 허용 — 없으면 R2 업로드 실패 |
| `src/test/resources/application.properties` | **신규** | 테스트용 더미 시크릿 + H2 |
| `infra/Dockerfile` | **신규** | ARM64 · Java 21 통일 · ffmpeg 포함 · 비루트 |
| `infra/docker-compose.yml` | **신규** | caddy + app + postgres |
| `infra/Caddyfile` | **신규** | CF Origin CA TLS, 1100MB 업로드 |
| `infra/setup.sh` | **신규** | VM 부트스트랩 (멱등) |
| `infra/backup.sh` / `restore.sh` | **신규** | pg_dump ↔ R2 |
| `infra/.env.example` | **신규** | 필수 환경변수 템플릿 |
| `.dockerignore` | **신규** | build/ 95MB 제외 |
| `Dockerfile.render`, `.dockerignore.render` | **삭제** | Java 21/24 불일치, Render 미사용 |

### ai-ott-web

| 파일 | 구분 | 내용 |
|---|---|---|
| `next.config.js` | 수정 | ngrok fallback 제거 → 운영에서 미설정 시 빌드 실패 |
| `config/env/requireApiBaseUrl.ts` | **신규** | 필수 환경변수 헬퍼 (타입을 `string` 으로 좁힘) |
| `config/env/production.ts`, `staging.ts` | 수정 | ngrok fallback 제거 |
| `app/constants.ts` | 수정 | `ngrok-skip-browser-warning` 헤더 제거 |
| `.env.production` | 수정 | `https://api.aiott.kr` |
| `wrangler.jsonc` | 수정 | `aiott.kr`, `www.aiott.kr` 커스텀 도메인 명시 |
| `.github/workflows/deploy-{development,staging,production}.yml` | **삭제** | Vercel 배포 (실제 미사용) |
| `.github/workflows/ci.yml` | 수정 | Node 22, lint 비차단 |

### ai-ott-admin

| 파일 | 구분 | 내용 |
|---|---|---|
| `next.config.ts` | 수정 | 운영 fallback 제거, opennext dev 초기화 |
| `src/lib/backend.ts` | 수정 | ngrok 헤더 제거 |
| `.env.production` | 수정 | `https://api.aiott.kr` |
| `.env.example` | **신규** | — |
| `wrangler.jsonc`, `open-next.config.ts` | **신규** | 배포 설정이 저장소에 없었다 |
| `package.json` | 수정 | `@opennextjs/cloudflare` + deploy 스크립트 |
| `pnpm-workspace.yaml` | 수정 | `onlyBuiltDependencies: esbuild, workerd` |
| `.github/workflows/deploy.yml` | **신규** | `pnpm run deploy` — **수동 실행만** (아래 주의 참고) |

### ai-ott-app (변경 없음)

`lib/config/constants.dart` 가 이미 `String.fromEnvironment('API_BASE_URL')` 를 쓴다.
빌드 시 주입만 하면 된다.

```bash
flutter build apk --dart-define=API_BASE_URL=https://api.aiott.kr
```

---

## 5. 엣지 케이스

| 상황 | 동작 |
|---|---|
| `JWT_SECRET` 미설정 | **기동 실패** — `PlaceholderResolutionException` (검증 완료) |
| `ADMIN_PASSWORD` 미설정 | **기동 실패** (검증 완료) |
| 운영 빌드에 `NEXT_PUBLIC_API_BASE_URL` 없음 | **빌드 실패** (검증 완료) |
| 토큰 없이 `/api/admin/**` | 401 (검증 완료) |
| ops 토큰으로 `/api/admin/**` | 401 — aud 불일치 (검증 완료) |
| 로그인 6회 연속 실패 | 429, 60초 후 해제 (검증 완료) |
| `/auth/login` 과 `/api/app/auth/login` | 같은 버킷 공유 → 경로 변경으로 우회 불가 (검증 완료) |
| `X-Forwarded-For` 위조 | CF-Connecting-IP 를 우선하므로 무효 |
| `aud=admin` 토큰으로 공개 GET 호출 | 401. 체인별 aud 가 다르므로 의도된 동작 |
| 트랜스코딩 중 컨테이너 재시작 | 진행 중 job 은 미완 상태로 남는다. **재시도는 어드민에서 수동** (`/admin/failures`) |
| R2 10GB 초과 | 업로드 실패. 용량 모니터링은 이번 범위 밖 |
| `R2_PRESIGN_PLAYBACK=true` 로 켬 | **재생 불가** — 세그먼트가 403. 반드시 false 유지 |
| `cdn.aiott.kr/source/...` 직접 접근 | 원본 mp4 가 같은 공개 버킷에 있다. 키에 UUID+타임스탬프+파일명이 들어가 추측은 어렵지만 노출 경로다 |
| Postgres 볼륨 손상 | `restore.sh` 로 최대 24시간 전 상태까지 복구 |
| Oracle 유휴 회수 | 재생성 후 30분 복구 절차 (§3) |
| `.env` 분실 | **복구 불가.** 유일한 수동 의존 — 별도 보관 필수 |

---

## 6. 사이드 이펙트

이번 변경으로 **기존 동작이 달라지는 것들**이다.

1. **어드민 로그인 자격증명이 바뀐다.** `admin`/`admin` 은 더 이상 동작하지 않는다.
   `.env` 의 `ADMIN_USERNAME`/`ADMIN_PASSWORD` 를 써야 한다.

2. **기존에 발급된 모든 토큰이 무효화된다.** `JWT_SECRET` 이 바뀌므로 재로그인이 필요하다.

3. **환경변수 누락 시 조용히 넘어가지 않는다.** 기동/빌드가 실패한다. 의도된 변경이지만,
   로컬 개발에서도 `JWT_SECRET` 등을 채워야 한다(테스트는 `src/test/resources` 가 처리).

4. **로컬 개발 시 어드민 API 호출에 토큰이 필요해졌다.** 이전에는 무인증으로 되던 요청이
   401 이 된다. `/auth/admin/login` 으로 토큰을 받아야 한다.

5. **`/actuator/mappings`, `/actuator/flyway` 가 닫힌다.** health 만 노출한다.

6. **`lazy-initialization: false`** 로 기동이 다소 느려진다 (측정: 약 15초).
   대신 설정 누락을 즉시 잡는다.

7. **Vercel 배포 워크플로가 사라진다.** Vercel 로 배포하던 경로는 애초에 쓰이지 않았지만,
   Vercel 프로젝트가 연결돼 있었다면 자동 배포가 멈춘다.

8. **`Dockerfile.render` 삭제.** Render 로 되돌리려면 `infra/Dockerfile` 을 참고해
   다시 만들어야 한다.

9. **영상 URL 도메인이 바뀐다** (`STORAGE_TYPE=r2`). 기존에 로컬 경로로 저장된
   레코드는 `cdn.aiott.kr` 로 자동 전환되지 않는다. 신규 콘텐츠부터 적용된다.

10. **`getPlaybackUrl` 이 더 이상 presigned URL 을 만들지 않는다**(기본값).
    이전 동작을 원하면 `R2_PRESIGN_PLAYBACK=true` 로 켤 수 있으나 재생이 깨진다.

---

## 7. 인프라 / 배포 고려사항

### 본인이 해야 하는 작업 (계정 소유자만 가능)

| # | 작업 | 비고 |
|---|---|---|
| 1 | Oracle Cloud 계정 + Ampere A1 인스턴스 생성 | 무료지만 **카드 검증 필수**. 리전 **춘천** |
| 2 | Cloudflare API 토큰 (R2 편집 + DNS 편집 + Workers 배포) | GH Secret `CLOUDFLARE_API_TOKEN`, `CLOUDFLARE_ACCOUNT_ID` |
| 3 | CF Origin CA 인증서 발급 → `infra/certs/` | 15년 만료, 1회로 끝 |
| 4 | R2 버킷 2개 생성 (`ai-ott-media`, `ai-ott-backups`) + `cdn.aiott.kr` 커스텀 도메인 | |
| 5 | Stripe 테스트 키 4개 | 베타는 테스트 모드로 충분 |
| 6 | SMTP 계정 (Resend 월 3,000통 / Brevo 일 300통 무료) | 없으면 `MAIL_ENABLED=false` |

### 배포 순서 (의존 관계 있음)

```
1. Oracle VM 생성 → SSH 접속 확인
2. git clone → cd infra → ./setup.sh          (docker 설치, 방화벽, 크론)
3. .env 작성 (필수 6개 값 비우면 setup.sh 가 중단시킨다)
4. CF Origin CA 인증서를 infra/certs/ 에 배치
5. docker compose up -d --build                → Flyway V1~V16 자동 적용
6. CF DNS: api A 레코드 → VM IP (프록시 ON)
7. CF SSL/TLS 모드 → Full (strict)             ← 이걸 빠뜨리면 502
8. R2 버킷 + cdn.aiott.kr 연결 → .env 의 R2_* 채우고 compose 재기동
9. 프론트 배포 — **web 은 자동**(CF Workers Builds 가 main push 를 감지),
   admin 은 상황 확인 후 (아래 주의)
10. CF Workers 에 admin.aiott.kr 커스텀 도메인 연결
11. 검증 체크리스트 (§8) 수행
```

### Oracle 특유의 함정

- **Ampere A1 용량 부족** — "out of host capacity" 로 생성이 자주 실패한다.
  춘천 → 서울 순으로 재시도.
- **iptables** — Oracle Ubuntu 이미지는 22번 외 인바운드를 기본 차단한다.
  콘솔의 보안 목록만 열어도 접속이 안 된다. `setup.sh` 가 80/443 을 열고
  `netfilter-persistent` 로 영구화한다.
- **유휴 회수** — 7일간 사용률이 낮으면 회수될 수 있다. 헬스체크 크론이 완화하지만
  보장은 아니다. 그래서 구성을 전부 코드로 남겼다.

### ⚠️ 프론트 배포 경로가 둘이 되지 않게

PR 검증 중 **`ai-ott-web` 저장소에 Cloudflare Workers Builds(대시보드 git 연동)가
붙어 있다는 사실이 확인됐다** — PR 에 `Workers Builds: ai-ott-web` 체크가 붙고 통과했다.
즉 그동안의 배포는 수동이 아니라 이 연동이 수행하고 있었다.

따라서 GitHub Actions 로 `wrangler deploy` 를 또 돌리면 같은 워커에 이중 배포가 된다.
- **web**: GH Actions 배포 워크플로를 두지 않는다. `ci.yml`(lint+build 검증)만 남긴다.
- **admin**: PR 에 CF 체크가 나타나지 않아 연동 여부가 불확실하다. 그래서
  `deploy.yml` 의 `push` 트리거를 주석 처리하고 **수동 실행만** 가능하게 뒀다.
  대시보드에서 Workers Builds 연동이 없음을 확인한 뒤 주석을 해제할 것.

`wrangler.jsonc` 에 추가한 커스텀 도메인 설정은 Workers Builds 가 빌드할 때도
그대로 적용된다.

### 오리진 보호

`api.aiott.kr` 의 CF 프록시를 켜면 오리진 IP 가 숨는다. Caddy 는 호스트명이 맞지
않는 요청(IP 직접 접근)에 응답하지 않는다. 더 강하게 막으려면 Oracle 보안 목록의
80/443 인그레스를 [Cloudflare IP 대역](https://www.cloudflare.com/ips/)으로 제한한다.

### 롤백

| 대상 | 방법 |
|---|---|
| 백엔드 | 이전 이미지 태그로 `docker compose up -d` (2세대 보관) |
| 프론트 | `wrangler rollback` (CF Workers 버전 롤백) |
| DB | Flyway 는 되돌리기가 없다 → `restore.sh` 로 백업 복원 |

---

## 8. 검증 방법

### 이미 검증한 항목 (로컬 arm64 + 실 Postgres)

Mac 이 arm64 라 Oracle Ampere A1 과 동일 아키텍처로 검증했다.

```
✅ ARM64 이미지 빌드 성공, 기동 15초
✅ Flyway 16개 마이그레이션 신규 DB 에 적용
✅ 무인증 /api/admin/{contents,users,channels,genres}      → 401
✅ 무인증 POST /api/admin/contents, DELETE /api/admin/users/x → 401
✅ 무인증 /api/ops/transcoding/summary                     → 401
✅ 공개 /health, /, /actuator/health, /api/app/feed        → 200
✅ admin/admin (옛 기본값) 로그인                          → 401
✅ 올바른 자격증명 → aud=["admin"], roles=["ROLE_ADMIN"] 토큰 발급
✅ 그 토큰으로 /api/admin/{contents,genres,auth/me}        → 200
✅ ops 토큰으로 /api/admin/contents                        → 401 (aud 격리)
✅ ops 토큰으로 /api/ops/transcoding/summary               → 200
✅ /auth/login 6회차                                       → 429
✅ /auth/admin/login 레이트리밋 신규 적용                   → 429
✅ /api/app/auth/login 이 같은 버킷 공유 (우회 차단)        → 429
✅ JWT_SECRET 없이 기동                                    → PlaceholderResolutionException
✅ ADMIN_PASSWORD 없이 기동                                → PlaceholderResolutionException
✅ gradlew clean build (테스트 포함)                       → BUILD SUCCESSFUL
✅ web 운영 빌드 성공, api.aiott.kr 주입 (89개 파일)
✅ web 빌드 산출물에 ngrok 잔재 0건 (실행 코드 기준)
✅ NEXT_PUBLIC_API_BASE_URL 없이 운영 빌드                 → 빌드 실패
✅ admin 빌드 성공
✅ R2 버킷 2개 생성(APAC), cdn.aiott.kr 커스텀 도메인 연결 후 실제 서빙 확인
✅ admin.aiott.kr → ai-ott-admin 워커 연결, HTTP 307 정상
⚠️ cdn.aiott.kr 은 CDN 캐싱이 되지 않는다(cf-cache-status: DYNAMIC).
   .txt / .ts 로 각각 실측했다. 어댑터가 Cache-Control 을 설정하지 않기 때문이다.
   R2 egress 는 캐싱과 무관하게 무료이므로 비용 문제는 없고, 매 요청이 R2 의
   Class B 오퍼레이션(월 1,000만 무료)을 소모한다. 베타 규모(예: 1,000뷰 ×
   150세그먼트 = 15만)에서는 여유가 크다.
```

#### Caddy 포함 전체 스택 + 실제 R2 로 E2E 검증

자체 서명 인증서로 caddy·app·postgres 3개 컨테이너를 모두 띄우고, 실제 R2
자격증명으로 업로드부터 재생까지 통과시켰다.

```
✅ Caddyfile 문법 검증 (caddy validate) — 최초에 log 블록 문법 오류가 있어 수정
✅ Caddy 경유 HTTPS: /health → 200, /api/admin/contents → 401
✅ 콘텐츠 생성 → 13MB mp4 업로드 → 트랜스코딩(약 24초) → status=READY
✅ hls_master_key = https://cdn.aiott.kr/hls/<assetId>/master.m3u8
✅ 마스터 플레이리스트 조회 → 200 (유효한 VOD 플레이리스트, 8세그먼트)
✅ ★ 세그먼트 4개 실제 로드 → 전부 200 (2.4~4.3MB 실데이터)
     presign 수정이 없었다면 이 지점에서 전부 403 이었다
✅ backup.sh → R2 업로드 성공
✅ 백업 회전(삭제) 경로 검증 — KEEP=1 로 강제해 3개 삭제 확인
✅ ★ 복원 리허설: content 전체 삭제 → restore.sh → 레코드·flyway 17건 복원,
     hls_master_key 까지 일치, 앱 재기동 후 /health 200
✅ 검증 후 R2 두 버킷과 로컬 볼륨 모두 정리
```

발견해 고친 것:
- **`ck_video_asset_storage` 가 `'R2'` 를 거부** → V17 마이그레이션 추가
- **Caddyfile `log` 블록 문법 오류** → Caddy 기동 실패였을 것
- **`backup.sh` 이식성 3건** (`date -Is`, `mapfile`, `head -n -N` 모두 GNU 전용)
  → Ubuntu 에서는 동작하지만 그러면 스크립트를 검증할 수 없다. 회전은 실패해도
  조용히 넘어가 백업이 쌓이는 쪽이라 검증 가능해야 한다.

알아 둘 동작:
- **업로드가 트랜스코딩을 자동으로 시작하지 않는다.** 자산 첨부 후
  `POST /api/admin/contents/{id}/transcode` 를 따로 호출해야 한다(어드민 UI 흐름과 동일).

### 배포 후 확인해야 하는 항목

```
□ 1.  curl https://api.aiott.kr/health → 200
□ 2.  docker compose logs app | grep -i flyway   (16개 적용)
□ 3.  CF SSL 모드 Full(strict), VM IP 직접 접근 차단
□ 4.  ★ admin/admin 로그인 실패
□ 5.  ★ 토큰 없이 POST /api/admin/contents → 401
□ 6.  ★ 로그인 6회 연속 → 429
□ 7.  E2E: 업로드 → 트랜스코딩 → R2 저장 → 재생 1건
□ 8.  cdn.aiott.kr 세그먼트에 cf-cache-status: HIT
□ 9.  CORS: aiott.kr 성공 / 임의 도메인 차단
□ 10. VM 재부팅 후 자동 기동 (restart: always)
□ 11. ★ backup.sh 실행 → R2 확인 → restore.sh 복원 리허설
□ 12. 전 저장소 ngrok 잔재 0건
```

4·5·6·11 번은 **"안 되는 것이 확인돼야 하는" 항목**이다. 되는 것만 확인하면
이번 같은 결함을 다시 놓친다. 11번은 복원해 보지 않은 백업은 백업이 아니기 때문이다.

---

## 9. 알려진 제한 / 후속 과제

| 항목 | 이유 | 우선도 |
|---|---|---|
| **ABR (다중 화질) 없음** | 단일 화질. 트랜스코딩 시간 3~4배 + R2 10GB 조기 소진 | 유료화 시점 |
| **서명 URL 없음** | presign 코드는 있으나 세그먼트를 보호하지 못해 껐다(기본 false). `cdn.aiott.kr` 주소를 알면 로그인 없이 시청 가능 | 유료화 시점 |
| **원본 mp4 가 공개 버킷에 있음** | `source/` 접두어로 같은 버킷에 저장된다. 비공개 버킷 분리가 정석 | 유료화 시점 |
| **트랜스코딩이 API 와 같은 프로세스** | 24GB 라 당장 문제없음. `TranscodingPort` 가 있어 분리는 국소적 | 트래픽 증가 시 |
| **단일 VM = 단일 장애점** | 무료 범위의 한계. 30분 복구 절차로 완화 | — |
| **lint 에러 (web 9 / admin 47)** | main 시점부터 존재. CI 를 막지 않도록 비차단 처리 | 별건 |
| **`middleware.ts` → `proxy.ts`** | Next.js 16 권장 명칭. 현재도 동작함 | 별건 |
| **`COMMERCIALIZATION_PLAN.md` 가 낡음** | 구현 완료 기능이 "없음" 으로 적혀 있고 Java 26 등 오기 | 별건 |
| **R2 용량 모니터링 없음** | 10GB 초과 시 업로드 실패를 사전에 알 수 없음 | 콘텐츠 유입 후 |
| **CDN 캐싱 미작동** | 어댑터가 `Cache-Control` 을 설정하지 않아 매 요청이 R2 에 도달한다. `PutObjectRequest` 3곳에 `.cacheControl(...)` 만 추가하면 된다. 단 재트랜스코딩이 같은 키를 재사용하므로 TTL 을 길게 잡으면 옛 세그먼트가 남을 수 있다 — TTL 선택이 필요해 이번 범위에서 제외 | 트래픽 증가 시 |
