# ai-ott-server

Spring Boot backend for the AI OTT streaming platform.

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 24 |
| Framework | Spring Boot 4.0.2 |
| Database | PostgreSQL (Flyway migrations) |
| Auth | JWT (HS256, hand-rolled) + Spring Security |
| Storage | Local filesystem or Cloudflare R2 (S3-compatible) |
| Video | FFmpeg (HLS transcoding) |
| Deploy | Render (Docker) |

---

## Local Development

### Prerequisites

- Java 24+
- Docker (for PostgreSQL) or a local PostgreSQL instance
- FFmpeg installed and on `PATH`

### 1. Clone and configure

```bash
git clone https://github.com/namyoungpark-2/ai-ott-server.git
cd ai-ott-server
```

Create a local `.env` or set environment variables (see [Environment Variables](#environment-variables)).
The app has sensible defaults for local dev — a PostgreSQL instance is expected at the default Render internal hostname, so override `DB_URL` for local:

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
| `DB_URL` | Render internal URL | JDBC URL |
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

## Deployment (Render)

The project ships with `Dockerfile.render` for Render's Docker runtime.

### Steps

1. **Create a Render Web Service**
   - Environment: **Docker**
   - Dockerfile path: `Dockerfile.render`
   - Instance type: Free (512 MB RAM)

2. **Create a Render PostgreSQL** database and link it, or set `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` manually.

3. **Set environment variables** in Render → Environment:

   ```
   JWT_SECRET=<long-random-secret>
   CORS_ALLOWED_ORIGINS=https://<your-frontend>.workers.dev
   DB_URL=<render-internal-postgres-url>
   DB_USERNAME=<db-user>
   DB_PASSWORD=<db-password>
   ```

   For R2 storage, also set:
   ```
   STORAGE_TYPE=r2
   R2_ACCOUNT_ID=...
   R2_ACCESS_KEY_ID=...
   R2_SECRET_ACCESS_KEY=...
   R2_BUCKET=...
   R2_PUBLIC_URL=...
   ```

4. **Deploy** — Render builds the Docker image and runs it. Flyway applies all migrations on startup.

5. **Health check** — Render pings `/` (returns 200 OK).

### Free plan notes

- The service spins down after 15 minutes of inactivity (cold start ~30s).
- RAM limit is 512 MB — JVM flags in `Dockerfile.render` are tuned accordingly.

---

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
