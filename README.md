# ai-ott-server

Spring Boot backend for the AI OTT streaming platform.

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| Database | PostgreSQL (Flyway migrations) |
| Auth | JWT (HS256, hand-rolled) + Spring Security |
| Storage | Local filesystem or Cloudflare R2 (S3-compatible) |
| Video | FFmpeg (HLS transcoding) |
| Deploy | Oracle Cloud VM (Docker Compose) — see [docs/infra/production-deployment.md](docs/infra/production-deployment.md) |

---

## Local Development

### Prerequisites

- Java 21+
- Docker (for PostgreSQL) or a local PostgreSQL instance
- FFmpeg installed and on `PATH`

### 1. Clone and configure

```bash
git clone https://github.com/namyoungpark-2/ai-ott-server.git
cd ai-ott-server
```

Create a local `.env` or set environment variables (see [Environment Variables](#environment-variables)).
필수 환경변수는 기본값이 없다(누락 시 기동 실패). 로컬에서도 아래 값을 설정해야 한다:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/aiott
export DB_USERNAME=aiott
export DB_PASSWORD=aiott_pw
export JWT_SECRET=local-dev-secret-change-me
```

### 2. Start PostgreSQL (Docker)

```bash
docker run -d \
  --name aiott-db \
  -e POSTGRES_DB=aiott \
  -e POSTGRES_USER=aiott \
  -e POSTGRES_PASSWORD=aiott_pw \
  -p 5432:5432 \
  postgres:16
```

### 3. Run

```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080**.
Flyway runs all migrations automatically on startup.

### 4. Build JAR

```bash
./gradlew bootJar
java -jar build/libs/ott-poc-0.0.1-SNAPSHOT.jar
```

---

## API Overview

### Auth (public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/signup` | Register a new user |
| POST | `/auth/login` | Login, returns JWT |
| POST | `/auth/admin/login` | Admin login (hardcoded: admin/admin) |
| POST | `/auth/ops/login` | Ops login (hardcoded: ops/ops) |

**Request body** (signup / login):
```json
{ "username": "alice", "password": "secret123" }
```

**Response**:
```json
{
  "accessToken": "<JWT>",
  "id": "<uuid>",
  "username": "alice",
  "role": "USER"
}
```

### App API (`/api/app/**`) — public by default

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/app/feed` | Home feed |
| GET | `/api/app/catalog/browse` | Catalog browse |
| GET | `/api/app/catalog/search` | Search |
| GET | `/api/app/contents/{id}` | Content detail |
| GET | `/api/app/playback/{id}` | Playback info (HLS URL) |

### Admin API (`/api/admin/**`) — requires ADMIN JWT

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/admin/contents` | Create content |
| POST | `/api/admin/uploads` | Upload video |
| GET | `/api/admin/video-assets` | List video assets |
| GET | `/api/admin/failures` | Transcoding failures |

Pass the JWT as `Authorization: Bearer <token>`.

---

## Database Migrations

Flyway manages the schema under `src/main/resources/db/migration/`.

| Version | Description |
|---------|-------------|
| V1 | Core catalog schema |
| V2 | Video asset & transcoding |
| V3 | Watch event analytics |
| V4 | Transcoding job indexes |
| V5 | Content status |
| V6 | Catalog taxonomy & discovery |
| V7 | Users table (signup/login) |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |
| `DB_URL` | (없음) | JDBC URL — compose 가 주입 |
| `DB_USERNAME` | `aiott` | DB username |
| `DB_PASSWORD` | `aiott_pw` | DB password |
| `JWT_SECRET` | `CHANGE_ME_...` | HS256 signing secret — **change in production** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed origins |
| `STORAGE_TYPE` | `local` | `local` or `r2` |
| `UPLOAD_DIR` | `./data/uploads` | Local upload directory |
| `HLS_DIR` | `./data/hls` | Local HLS output directory |
| `FFMPEG_PATH` | `ffmpeg` | Path to FFmpeg binary |
| `R2_ACCOUNT_ID` | — | Cloudflare account ID (R2 mode) |
| `R2_ACCESS_KEY_ID` | — | R2 access key (R2 mode) |
| `R2_SECRET_ACCESS_KEY` | — | R2 secret key (R2 mode) |
| `R2_BUCKET` | — | R2 bucket name (R2 mode) |
| `R2_PUBLIC_URL` | — | Public base URL for R2 assets |

---

## Deployment

배포는 Oracle Cloud Always Free VM 에서 Docker Compose 로 운영한다.
전체 설계·순서·검증 절차는 **[docs/infra/production-deployment.md](docs/infra/production-deployment.md)** 를 참고한다.

```bash
# VM 에서
git clone https://github.com/namyoungpark-2/ai-ott-server.git
cd ai-ott-server/infra
./setup.sh              # docker 설치, 방화벽, 백업 크론 (멱등)
cp .env.example .env    # 값 채우기 — 필수 6개가 비면 setup.sh 가 중단시킨다
docker compose up -d --build
```

이전에는 `Dockerfile.render` 로 Render 배포를 계획했으나 실제로 배포된 적이 없고,
`build.gradle` 의 toolchain(21)과 이미지의 JDK(24)가 어긋나 있었다.
`infra/Dockerfile` 로 대체하고 삭제했다.

## Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)**:

```
adapter/in/web/         ← REST controllers
adapter/out/persistence ← JPA repositories & entities
adapter/out/storage/    ← Local / R2 storage adapters
application/port/in/    ← Use case interfaces
application/port/out/   ← Output port interfaces
application/service/    ← Business logic
config/                 ← Spring config, security, CORS
domain/model/           ← Domain enums and value objects
```

Storage is selected at startup via `app.storage.type`:
- `local` — files served from `./data/` via Spring static handlers
- `r2` — files uploaded to Cloudflare R2, served via public R2 URL
