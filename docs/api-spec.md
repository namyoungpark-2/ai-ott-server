# AI OTT Server — API Specification

> Base URL: `http://localhost:8080`
> No context-path prefix — all routes start at root.

---

## Table of Contents

- [Authentication](#authentication)
- [Health](#1-health)
- [Auth](#2-auth)
- [Admin Auth](#3-admin-auth)
- [Admin Users](#4-admin-users)
- [User Profile (Me)](#5-user-profile-me)
- [Payments](#6-payments)
- [Stripe Webhook](#7-stripe-webhook)
- [Content](#8-content)
- [Series](#9-series)
- [Seasons](#10-seasons)
- [Feed](#11-feed)
- [Catalog](#12-catalog)
- [Playback](#13-playback)
- [Watch Analytics](#14-watch-analytics)
- [Watch Events](#15-watch-events)
- [Admin Content](#16-admin-content)
- [Admin Content Metadata](#17-admin-content-metadata)
- [Admin Video Assets](#18-admin-video-assets)
- [Admin Transcoding](#19-admin-transcoding)
- [Admin Categories](#20-admin-categories)
- [Admin Genres](#21-admin-genres)
- [Admin Failures](#22-admin-failures)
- [Admin Uploads](#23-admin-uploads)
- [Ops Transcoding](#24-ops-transcoding)
- [Creator Channel](#25-creator-channel)
- [Creator Content](#26-creator-content)
- [Creator Series](#27-creator-series)
- [Channel Public](#28-channel-public)
- [Channel Subscription](#29-channel-subscription)
- [Admin Channels](#30-admin-channels)

---

## Authentication

JWT 기반 인증. 세 가지 토큰 타입이 존재한다:

| Type | 용도 | 전달 방식 |
|------|------|-----------|
| **User** | 일반 사용자 | `Authorization: Bearer <token>` 또는 쿠키 |
| **Admin** | 관리자 | `Authorization: Bearer <token>` |
| **Ops** | 운영 모니터링 | `Authorization: Bearer <token>` |

### 접근 권한 요약

| 구분 | 경로 패턴 |
|------|-----------|
| Public | `/`, `/health`, `/auth/*`, `/api/app/contents/*`, `/api/app/series/*`, `/api/app/seasons/*`, `/api/app/feed`, `/api/app/catalog/*`, `/api/app/playback/*`, `/api/app/analytics/*`, `/api/app/watch-events`, `/api/webhooks/stripe` |
| User JWT 필요 | `/api/app/me/*`, `/api/app/payments/*`, `/api/app/creator/*`, `/api/app/channels/*/subscribe` |
| Admin JWT 필요 | `/api/admin/*` |
| Ops JWT 필요 | `/api/ops/*` |

---

## 1. Health

| Method | Endpoint | Auth | Response | Description |
|--------|----------|------|----------|-------------|
| `GET` | `/` | - | 200 OK | Health check |
| `GET` | `/health` | - | 200 OK | Health check |

---

## 2. Auth

Base path: `/auth`

### POST /auth/signup

사용자 회원가입.

```json
// Request
{ "username": "string", "password": "string" }

// Response 200
{ "accessToken": "string", "id": "long", "username": "string", "role": "string", "subscriptionTier": "string" }
```

### POST /auth/login

사용자 로그인.

```json
// Request
{ "username": "string", "password": "string" }

// Response 200
{ "accessToken": "string", "id": "long", "username": "string", "role": "string", "subscriptionTier": "string" }
```

### POST /auth/logout

로그아웃 (인증 쿠키 삭제).

```
Response 200 OK
```

### POST /auth/verify-email

이메일 인증.

```json
// Request
{ "token": "string" }

// Response 200
{ "message": "string" }
```

### POST /auth/resend-verification

인증 이메일 재발송.

```json
// Request
{ "username": "string" }

// Response 200
{ "message": "string" }
```

### POST /auth/forgot-password

비밀번호 재설정 요청.

```json
// Request
{ "username": "string" }

// Response 200
{ "message": "string" }
```

### POST /auth/reset-password

비밀번호 재설정.

```json
// Request
{ "token": "string", "newPassword": "string" }

// Response 200
{ "message": "string" }
```

### POST /auth/admin/login

관리자 로그인 (하드코딩된 자격증명).

```json
// Request
{ "username": "string", "password": "string" }

// Response 200
{ "accessToken": "string", "id": "long", "username": "string", "role": "string", "subscriptionTier": "string" }
```

### POST /auth/ops/login

운영자 로그인 (하드코딩된 자격증명).

```json
// Request
{ "username": "string", "password": "string" }

// Response 200
{ "accessToken": "string" }
```

---

## 3. Admin Auth

Base path: `/api/admin/auth`

### GET /api/admin/auth/me

현재 관리자 정보 조회.

```json
// Response 200
{ "id": "long", "email": "string", "name": "string", "role": "string" }
```

---

## 4. Admin Users

Base path: `/api/admin/users` | Auth: **Admin**

### GET /api/admin/users

사용자 목록 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `tier` | string | No | 구독 티어로 필터링 |

```json
// Response 200
[{ "id": "long", "username": "string", "role": "string", "subscriptionTier": "string", "..." }]
```

### PUT /api/admin/users/{id}/subscription

사용자 구독 티어 변경.

```json
// Request
{ "tier": "string" }

// Response 200
{ "message": "string" }
```

### DELETE /api/admin/users/{id}

사용자 삭제 (관련 시청 데이터 포함).

```
Response 200 { "message": "string" }
```

---

## 5. User Profile (Me)

Base path: `/api/app/me` | Auth: **User JWT**

### GET /api/app/me/continue-watching

이어보기 목록 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
[{ "contentId": "long", "title": "string", "positionMs": "long", "durationMs": "long", "..." }]
```

### GET /api/app/me/playback-progress/{contentId}

특정 콘텐츠의 재생 위치 조회.

```json
// Response 200
{ "positionMs": "long" }
```

### POST /api/app/me/playback-progress/{contentId}

재생 위치 저장.

```json
// Request
{ "positionMs": "long", "durationMs": "long" }

// Response 200
{ "message": "string" }
```

---

## 6. Payments

Base path: `/api/app/payments` | Auth: **User JWT**

### POST /api/app/payments/checkout

Stripe 결제 세션 생성.

```json
// Request
{ "plan": "BASIC | PREMIUM" }

// Response 200
{ "url": "string" }
```

### POST /api/app/payments/portal

Stripe 고객 포털 열기.

```json
// Request
{ "stripeCustomerId": "string" }

// Response 200
{ "url": "string" }
```

---

## 7. Stripe Webhook

### POST /api/webhooks/stripe

Stripe 웹훅 수신. `Stripe-Signature` 헤더로 서명 검증.

| Header | Required | Description |
|--------|----------|-------------|
| `Stripe-Signature` | Yes | Stripe 서명 |

```
Body: Raw JSON payload
Response 200 { "message": "string" }
```

---

## 8. Content

Base path: `/api/app/contents` | Auth: **Public**

### GET /api/app/contents/{contentId}

콘텐츠 상세 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
{
  "id": "long",
  "title": "string",
  "description": "string",
  "type": "string",
  "status": "string",
  "genres": ["string"],
  "categories": ["string"],
  "..."
}
```

### GET /api/app/contents/{contentId}/related

관련 콘텐츠 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |
| `types` | string | No | TRAILER, CLIP, EXTRA (콤마 구분) |

```json
// Response 200
[{ "id": "long", "title": "string", "type": "string", "..." }]
```

---

## 9. Series

Base path: `/api/app/series` | Auth: **Public**

### GET /api/app/series/{seriesId}

시리즈 상세 조회 (에피소드 포함).

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
{
  "id": "long",
  "title": "string",
  "seasons": [{
    "seasonId": "long",
    "seasonNumber": "int",
    "episodes": [{ "id": "long", "title": "string", "episodeNumber": "int", "..." }]
  }]
}
```

---

## 10. Seasons

Base path: `/api/app/seasons` | Auth: **Public**

### GET /api/app/seasons/{seasonId}/episodes

시즌별 에피소드 목록 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
[{ "id": "long", "title": "string", "episodeNumber": "int", "..." }]
```

---

## 11. Feed

Base path: `/api/app` | Auth: **Public**

### GET /api/app/feed

콘텐츠 피드 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
[{ "id": "long", "title": "string", "thumbnailUrl": "string", "..." }]
```

---

## 12. Catalog

Base path: `/api/app/catalog` | Auth: **Public**

### GET /api/app/catalog/browse

카테고리별 카탈로그 탐색.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `sectionLimit` | int | No | 12 | 섹션당 아이템 수 (max: 30) |

```json
// Response 200
{
  "sections": [{
    "category": "string",
    "items": [{ "id": "long", "title": "string", "..." }]
  }]
}
```

### GET /api/app/catalog/search

카탈로그 검색.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `q` | string | No | - | 검색어 |
| `category` | string | No | - | 카테고리 필터 |
| `genre` | string | No | - | 장르 필터 |
| `limit` | int | No | 24 | 결과 수 (1-100) |
| `offset` | int | No | 0 | 페이지 오프셋 |

```json
// Response 200
{
  "items": [{ "id": "long", "title": "string", "..." }],
  "total": "int"
}
```

---

## 13. Playback

Base path: `/api/app/playback` | Auth: **Public**

### GET /api/app/playback/{contentId}/playback

재생 정보 조회 (HLS 스트림 URL 등).

```json
// Response 200
{
  "hlsUrl": "string",
  "durationMs": "long",
  "..."
}
```

---

## 14. Watch Analytics

Base path: `/api/app/analytics` | Auth: **Public**

### GET /api/app/analytics/contents/{contentId}/daily

콘텐츠 일별 시청 통계.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `from` | ISO date | Yes | 시작일 |
| `to` | ISO date | Yes | 종료일 |

```json
// Response 200
{
  "contentId": "long",
  "daily": [{ "date": "string", "watchTimeMs": "long", "viewCount": "int" }]
}
```

### GET /api/app/analytics/contents/top

인기 콘텐츠 순위.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `from` | ISO date | Yes | - | 시작일 |
| `to` | ISO date | Yes | - | 종료일 |
| `metric` | string | No | watchTimeMs | 정렬 기준 |
| `limit` | int | No | 10 | 결과 수 |

```json
// Response 200
{
  "items": [{ "contentId": "long", "title": "string", "watchTimeMs": "long", "viewCount": "int" }]
}
```

---

## 15. Watch Events

Base path: `/api/app/watch-events` | Auth: **Public**

### POST /api/app/watch-events

시청 이벤트 수집.

```json
// Request
{ "contentId": "long", "userId": "long", "positionMs": "long", "..." }

// Response 200
{ "..." }
```

---

## 16. Admin Content

Base path: `/api/admin/contents` | Auth: **Admin**

### POST /api/admin/contents

콘텐츠 생성.

```json
// Request
{ "title": "string", "description": "string", "type": "string", "..." }

// Response 200
{ "id": "long", "..." }
```

### GET /api/admin/contents

콘텐츠 목록 조회.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `status` | string | No | - | 상태 필터 |
| `limit` | int | No | 50 | 결과 수 |

```json
// Response 200
[{ "id": "long", "title": "string", "status": "string", "..." }]
```

### GET /api/admin/contents/{contentId}

콘텐츠 상세 조회.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

### POST /api/admin/contents/{contentId}/assets

비디오 에셋 첨부 (Multipart).

```
Content-Type: multipart/form-data
Part: file (binary)
```

```json
// Response 200
{ "videoAssetId": "long", "..." }
```

### PATCH /api/admin/contents/{contentId}/status

콘텐츠 상태 변경.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `status` | string | Yes | 변경할 상태 |

```
Response 200 OK
```

### POST /api/admin/contents/{contentId}/transcode

트랜스코딩 시작.

```
Response 202 Accepted
```

---

## 17. Admin Content Metadata

Base path: `/api/admin/contents` | Auth: **Admin**

### PUT /api/admin/contents/{contentId}/metadata

콘텐츠 메타데이터 수정.

```json
// Request
{ "title": "string", "description": "string", "lang": "string", "..." }

// Response 200 OK
```

### PUT /api/admin/contents/{contentId}/taxonomy

콘텐츠 분류 수정 (장르, 카테고리 등).

```json
// Request
{ "genreIds": ["long"], "categoryIds": ["long"], "iabCategoryIds": ["string"], "..." }

// Response 200 OK
```

---

## 18. Admin Video Assets

Base path: `/api/admin/video-assets` | Auth: **Admin**

### GET /api/admin/video-assets

비디오 에셋 목록 조회.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `status` | string | No | - | 상태 필터 |
| `limit` | int | No | 50 | 결과 수 |

### GET /api/admin/video-assets/{videoAssetId}

비디오 에셋 상세 조회.

---

## 19. Admin Transcoding

Base path: `/api/admin/video-assets` | Auth: **Admin**

### POST /api/admin/video-assets/{videoAssetId}/transcode

수동 트랜스코딩 시작.

```
Response 200 OK
```

### POST /api/admin/video-assets/{videoAssetId}/retry

실패한 트랜스코딩 재시도.

```json
// Response 200
{ "..." }
```

---

## 20. Admin Categories

Base path: `/api/admin/categories` | Auth: **Admin**

### POST /api/admin/categories

카테고리 생성.

```json
// Request
{ "name": "string", "..." }

// Response 200
{ "id": "long", "name": "string" }
```

### GET /api/admin/categories

카테고리 목록 조회.

```json
// Response 200
[{ "id": "long", "name": "string" }]
```

---

## 21. Admin Genres

Base path: `/api/admin/genres` | Auth: **Admin**

### POST /api/admin/genres

장르 생성.

```json
// Request
{ "name": "string", "..." }

// Response 200
{ "id": "long", "name": "string" }
```

### GET /api/admin/genres

장르 목록 조회.

```json
// Response 200
[{ "id": "long", "name": "string" }]
```

---

## 22. Admin Failures

Base path: `/api/admin/failures` | Auth: **Admin**

### GET /api/admin/failures

실패 대시보드 — 에셋/작업 상태 포함 콘텐츠 목록.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `limit` | int | No | 200 | 결과 수 |

```json
// Response 200
[{ "contentId": "long", "title": "string", "assetStatus": "string", "jobStatus": "string", "..." }]
```

---

## 23. Admin Uploads

Base path: `/api/admin/uploads` | Auth: **Admin**

### POST /api/admin/uploads/uploads

통합 업로드 (콘텐츠 생성 또는 기존 콘텐츠에 첨부).

```
Content-Type: multipart/form-data
Parts:
  - file (binary, required)
  - contentId (string, optional — 기존 콘텐츠에 첨부 시)
  - title (string)
  - mode (string)
  - seriesId (string, optional)
  - seriesTitle (string, optional)
  - seasonNumber (int, optional)
  - episodeNumber (int, optional)
```

```json
// Response 200
{ "contentId": "long", "videoAssetId": "long", "..." }
```

---

## 24. Ops Transcoding

Base path: `/api/ops/transcoding` | Auth: **Ops**

### GET /api/ops/transcoding/summary

트랜스코딩 요약 통계.

### GET /api/ops/transcoding/failures/top

상위 실패 항목.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `limit` | int | No | 5 | 결과 수 |

### GET /api/ops/transcoding/recent

최근 트랜스코딩 작업 목록.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `limit` | int | No | 20 | 결과 수 |

---

## 25. Creator Channel

Base path: `/api/app/creator/channel` | Auth: **User JWT**

### GET /api/app/creator/channel

내 채널 정보 조회 (채널이 없으면 자동 생성).

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
{
  "id": "uuid",
  "handle": "user-abc12345",
  "name": "user-abc12345",
  "description": null,
  "profileImageUrl": null,
  "bannerImageUrl": null,
  "isOfficial": false,
  "subscriberCount": 0,
  "status": "ACTIVE",
  "createdAt": "2026-03-28T13:00:00Z"
}
```

### PUT /api/app/creator/channel

채널 정보 수정.

```json
// Request
{ "name": "string", "description": "string", "profileImageUrl": "string", "bannerImageUrl": "string" }

// Response 200
{ "id": "uuid", "handle": "string", "name": "string", "..." }
```

---

## 26. Creator Content

Base path: `/api/app/creator/contents` | Auth: **User JWT**

### POST /api/app/creator/contents

콘텐츠 생성 (업로드 후 자동 PUBLISHED).

```json
// Request
{
  "mode": "MOVIE | EPISODE",
  "title": "string",
  "seriesId": "uuid (EPISODE only)",
  "seriesTitle": "string (새 시리즈 생성 시)",
  "seasonNumber": 1,
  "episodeNumber": 1
}

// Response 200
{ "contentId": "uuid" }
```

### GET /api/app/creator/contents

내 콘텐츠 목록.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `limit` | int | No | 50 | 결과 수 |

```json
// Response 200
[{
  "contentId": "uuid",
  "title": "string",
  "contentType": "MOVIE",
  "status": "PUBLISHED",
  "videoAssetStatus": "READY",
  "thumbnailUrl": null,
  "createdAt": "2026-03-28T13:00:00Z"
}]
```

### PUT /api/app/creator/contents/{id}/metadata

메타데이터 수정.

```json
// Request
{ "title": "string", "description": "string", "lang": "en" }

// Response 200
{ "message": "updated" }
```

### PATCH /api/app/creator/contents/{id}/status

콘텐츠 공개/비공개 전환.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `status` | string | Yes | PUBLISHED, UNLISTED, ARCHIVED |

### POST /api/app/creator/contents/{id}/upload

비디오 파일 업로드 (소유권 검증 후 트랜스코딩 자동 시작).

```
Content-Type: multipart/form-data
Part: file (binary)
```

```json
// Response 200
{ "contentId": "uuid", "videoAssetId": "uuid", "status": "PROCESSING" }
```

### DELETE /api/app/creator/contents/{id}

콘텐츠 삭제 (ARCHIVED로 소프트 삭제).

---

## 25.5 Creator Channel — Handle

### PATCH /api/app/creator/channel/handle

채널 handle 변경.

```json
// Request
{ "handle": "my-new-handle" }

// Response 200
{ "message": "handle updated", "handle": "my-new-handle" }
```

---

## 29.5 Channel Subscription Status

### GET /api/app/channels/{handle}/subscription-status

구독 여부 확인 (JWT 필수).

```json
// Response 200
{ "subscribed": true }
```

---

## 27. Creator Series

Base path: `/api/app/creator/series` | Auth: **User JWT**

### POST /api/app/creator/series

시리즈 생성.

```json
// Request
{ "title": "string", "description": "string" }

// Response 200
{ "seriesId": "uuid", "title": "string", "description": "string", "episodeCount": 0 }
```

### GET /api/app/creator/series

내 시리즈 목록.

### PUT /api/app/creator/series/{id}

시리즈 수정.

```json
// Request
{ "title": "string", "description": "string", "lang": "en" }
```

---

## 28. Channel Public

Base path: `/api/app/channels` | Auth: **Public**

### GET /api/app/channels/{handle}

채널 페이지 조회.

```json
// Response 200
{
  "id": "uuid",
  "handle": "official",
  "name": "AI OTT Official",
  "description": "string",
  "profileImageUrl": "string",
  "bannerImageUrl": "string",
  "isOfficial": true,
  "subscriberCount": 1234,
  "status": "ACTIVE",
  "createdAt": "2026-03-28T13:00:00Z"
}
```

### GET /api/app/channels/{handle}/contents

채널 콘텐츠 목록 (PUBLISHED만).

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `lang` | string | No | - | 언어 코드 |
| `limit` | int | No | 24 | 결과 수 |
| `offset` | int | No | 0 | 페이지 오프셋 |

### GET /api/app/channels/{handle}/series

채널 시리즈 목록.

---

## 29. Channel Subscription

Auth: **User JWT**

### POST /api/app/channels/{handle}/subscribe

채널 구독.

```json
// Response 200
{ "message": "subscribed" }
```

### DELETE /api/app/channels/{handle}/subscribe

구독 해제.

```json
// Response 200
{ "message": "unsubscribed" }
```

### GET /api/app/me/subscriptions

내 구독 채널 목록.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `lang` | string | No | 언어 코드 |

```json
// Response 200
[{ "id": "uuid", "handle": "string", "name": "string", "profileImageUrl": "string", "isOfficial": false, "subscriberCount": 42, "status": "ACTIVE" }]
```

---

## 30. Admin Channels

Base path: `/api/admin/channels` | Auth: **Admin**

### GET /api/admin/channels

전체 채널 목록.

| Query Param | Type | Required | Default | Description |
|-------------|------|----------|---------|-------------|
| `limit` | int | No | 50 | 결과 수 |

### PATCH /api/admin/channels/{id}/status

채널 정지/활성화.

| Query Param | Type | Required | Description |
|-------------|------|----------|-------------|
| `status` | string | Yes | ACTIVE, SUSPENDED |

---

## Common Information

### 공통 파라미터

| Param | Description |
|-------|-------------|
| `lang` | 다국어 지원 언어 코드. `LangResolver`로 처리됨 |

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | 성공 |
| 202 | 비동기 작업 수락 (트랜스코딩 등) |
| 400 | 요청 유효성 검증 실패 |
| 401 | 인증 실패 |
| 409 | 리소스 충돌 (이미 존재) |
| 500 | 서버 내부 오류 |

### CORS

허용 오리진: `app.cors.allowed-origins` 설정 값 (기본: `localhost:3000`, `ai-ott-admin.jarneg23.workers.dev`, `ai-ott-web.jarneg23.workers.dev`)

허용 메서드: GET, POST, PUT, DELETE, PATCH, OPTIONS

### File Upload

- 최대 파일 크기: 1024MB
- 최대 요청 크기: 1024MB
