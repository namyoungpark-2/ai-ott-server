# AI OTT Web — Channel System Integration Guide

> ai-ott-web 프론트엔드에서 Creator & Channel 시스템을 연동하기 위한 가이드
> API Base URL: http://localhost:8080 (local)
> 관련 API 문서: ../api-spec.md (Section 25~30)

---

## 1. 채널 페이지 구현

사용자가 특정 크리에이터의 채널을 방문했을 때 보여줄 페이지.

### 라우트

```
/channels/:handle    → 채널 메인 페이지
```

### API 호출

```typescript
// 1. 채널 정보 조회
GET /api/app/channels/{handle}?lang=ko

// Response
{
  "id": "uuid",
  "handle": "official",
  "name": "AI OTT Official",
  "description": "Official AI OTT platform channel",
  "profileImageUrl": "https://...",   // null일 수 있음 → 기본 아바타
  "bannerImageUrl": "https://...",    // null일 수 있음 → 기본 배너
  "isOfficial": true,                 // 공식 채널 뱃지 표시용
  "subscriberCount": 1234,
  "status": "ACTIVE",
  "createdAt": "2026-03-28T13:00:00Z"
}

// 2. 채널 콘텐츠 목록 (페이징)
GET /api/app/channels/{handle}/contents?lang=ko&limit=24&offset=0

// Response
[
  {
    "contentId": "uuid",
    "title": "영상 제목",
    "contentType": "MOVIE",          // MOVIE | EPISODE
    "status": "PUBLISHED",
    "thumbnailUrl": "https://...",    // null일 수 있음 → poster_url 매핑
    "createdAt": "2026-03-28T13:00:00Z"
  }
]

// 3. 채널 시리즈 목록
GET /api/app/channels/{handle}/series?lang=ko

// Response
[
  {
    "seriesId": "uuid",
    "title": "시리즈 제목",
    "status": "PUBLISHED",
    "episodeCount": 12
  }
]
```

### UI 구성 요소

```
┌──────────────────────────────────────────────────┐
│ [배너 이미지 - bannerImageUrl]                    │
│                                                    │
│  [프로필] 채널명          구독자 1,234명            │
│           @handle         [구독] 버튼               │
│           채널 설명...                              │
├──────────────────────────────────────────────────┤
│ [탭: 영상 | 시리즈]                                │
│                                                    │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐      │
│ │썸네일   │ │썸네일   │ │썸네일   │ │썸네일   │      │
│ │제목     │ │제목     │ │제목     │ │제목     │      │
│ │MOVIE    │ │EPISODE │ │MOVIE    │ │MOVIE    │      │
│ └────────┘ └────────┘ └────────┘ └────────┘      │
│                                                    │
│ [더 보기] (offset += limit)                        │
└──────────────────────────────────────────────────┘
```

### 구현 체크리스트

- [ ] 채널 페이지 라우트 추가 (`/channels/:handle`)
- [ ] 채널 헤더 컴포넌트 (배너, 프로필, 이름, 구독자수, 구독 버튼)
- [ ] `isOfficial === true`이면 공식 채널 뱃지 표시
- [ ] 콘텐츠 그리드 (썸네일, 제목, 타입, 날짜)
- [ ] 무한 스크롤 또는 "더 보기" 페이징 (`offset` 증가)
- [ ] 시리즈 탭 (시리즈 목록 + 에피소드 수)
- [ ] 콘텐츠 클릭 → 기존 재생 페이지로 이동
- [ ] `thumbnailUrl`이 null일 때 기본 썸네일 표시
- [ ] `profileImageUrl`이 null일 때 기본 아바타 표시
- [ ] `bannerImageUrl`이 null일 때 기본 그라데이션 배너

---

## 2. 구독 기능

### API 호출

```typescript
// 구독 (로그인 필수)
POST /api/app/channels/{handle}/subscribe
Authorization: Bearer <user-jwt>

// Response 200
{ "message": "subscribed" }

// 구독 해제
DELETE /api/app/channels/{handle}/subscribe
Authorization: Bearer <user-jwt>

// Response 200
{ "message": "unsubscribed" }

// 내 구독 목록
GET /api/app/me/subscriptions?lang=ko
Authorization: Bearer <user-jwt>

// Response 200
[
  {
    "id": "uuid",
    "handle": "official",
    "name": "AI OTT Official",
    "profileImageUrl": "https://...",
    "isOfficial": true,
    "subscriberCount": 1234,
    "status": "ACTIVE"
  }
]
```

### 구독 버튼 상태 관리

현재 서버에 별도의 "구독 여부 확인" 단일 API는 없음. 두 가지 방법:

**방법 A (추천):** 내 구독 목록을 로그인 시 가져와서 클라이언트에 캐시
```typescript
// 로그인 후 한번 호출
const subscriptions = await fetch('/api/app/me/subscriptions');
const subscribedHandles = new Set(subscriptions.map(s => s.handle));

// 채널 페이지에서 확인
const isSubscribed = subscribedHandles.has(channelHandle);
```

**방법 B:** 채널 페이지 로드 시 내 구독 목록에서 해당 handle 존재 여부 확인

### 구현 체크리스트

- [ ] 구독/구독해제 API 호출 함수
- [ ] 구독 버튼 상태 관리 (구독됨/안됨)
- [ ] 구독 후 subscriberCount 로컬 업데이트 (+1 / -1)
- [ ] 비로그인 시 구독 버튼 → 로그인 페이지로 리다이렉트
- [ ] 내 구독 목록 페이지 (선택사항)

---

## 3. 크리에이터 스튜디오 (내 채널 관리)

로그인한 사용자가 자신의 채널과 콘텐츠를 관리하는 대시보드.

### 라우트

```
/studio              → 크리에이터 스튜디오 메인
/studio/channel      → 채널 설정
/studio/contents     → 내 콘텐츠 목록
/studio/contents/new → 콘텐츠 업로드
/studio/series       → 내 시리즈 관리
```

### API 호출 — 채널 관리

```typescript
// 내 채널 조회 (없으면 자동 생성됨)
GET /api/app/creator/channel?lang=ko
Authorization: Bearer <user-jwt>

// Response 200
{
  "id": "uuid",
  "handle": "user-abc12345",     // 자동 생성된 handle
  "name": "user-abc12345",       // 초기값 = handle
  "description": null,
  "profileImageUrl": null,
  "bannerImageUrl": null,
  "isOfficial": false,
  "subscriberCount": 0,
  "status": "ACTIVE",
  "createdAt": "2026-03-28T13:00:00Z"
}

// 채널 정보 수정
PUT /api/app/creator/channel
Authorization: Bearer <user-jwt>
Content-Type: application/json

{
  "name": "내 멋진 채널",
  "description": "채널 소개글",
  "profileImageUrl": "https://...",    // 이미지 업로드는 별도 처리 필요
  "bannerImageUrl": "https://..."
}
```

### API 호출 — 콘텐츠 관리

```typescript
// 콘텐츠 생성 (메타데이터만 — 파일 업로드는 기존 asset API 사용)
POST /api/app/creator/contents
Authorization: Bearer <user-jwt>
Content-Type: application/json

// 단독 영상
{ "mode": "MOVIE", "title": "내 첫 영상" }

// 에피소드 (새 시리즈 생성)
{
  "mode": "EPISODE",
  "title": "에피소드 1",
  "seriesTitle": "내 시리즈",
  "seasonNumber": 1,
  "episodeNumber": 1
}

// 에피소드 (기존 시리즈에 추가)
{
  "mode": "EPISODE",
  "title": "에피소드 2",
  "seriesId": "uuid",
  "seasonNumber": 1,
  "episodeNumber": 2
}

// Response 200
{ "contentId": "uuid" }

// 내 콘텐츠 목록
GET /api/app/creator/contents?lang=ko&limit=50
Authorization: Bearer <user-jwt>

// Response 200
[
  {
    "contentId": "uuid",
    "title": "내 첫 영상",
    "contentType": "MOVIE",
    "status": "PUBLISHED",            // DRAFT | PUBLISHED | UNLISTED | ARCHIVED
    "videoAssetStatus": "READY",      // UPLOADED | TRANSCODING | READY | FAILED | null
    "thumbnailUrl": null,
    "createdAt": "2026-03-28T13:00:00Z"
  }
]

// 메타데이터 수정
PUT /api/app/creator/contents/{id}/metadata
Authorization: Bearer <user-jwt>

{ "title": "수정된 제목", "description": "설명 추가", "lang": "ko" }

// 상태 변경
PATCH /api/app/creator/contents/{id}/status?status=UNLISTED
Authorization: Bearer <user-jwt>

// 삭제 (ARCHIVED로 소프트 삭제)
DELETE /api/app/creator/contents/{id}
Authorization: Bearer <user-jwt>
```

### API 호출 — 시리즈 관리

```typescript
// 시리즈 생성
POST /api/app/creator/series
Authorization: Bearer <user-jwt>

{ "title": "내 시리즈", "description": "시리즈 설명" }

// Response 200
{ "seriesId": "uuid", "title": "내 시리즈", "description": "시리즈 설명", "episodeCount": 0 }

// 시리즈 목록
GET /api/app/creator/series?lang=ko
Authorization: Bearer <user-jwt>

// 시리즈 수정
PUT /api/app/creator/series/{id}
Authorization: Bearer <user-jwt>

{ "title": "수정된 시리즈명", "description": "수정된 설명", "lang": "ko" }
```

### 콘텐츠 업로드 플로우

현재 비디오 파일 업로드는 기존 Admin API를 통해 처리됨. 크리에이터 전용 파일 업로드 API는 Task 8에서 통합 예정. 당장은:

```
Step 1: POST /api/app/creator/contents → contentId 생성
Step 2: POST /api/admin/contents/{contentId}/assets (file multipart)  ← 기존 Admin API 재사용
Step 3: 트랜스코딩 자동 시작
Step 4: videoAssetStatus가 READY로 바뀌면 재생 가능
```

> **TODO:** 크리에이터 전용 파일 업로드 엔드포인트 (`POST /api/app/creator/contents/{id}/upload`) 추가 필요. 현재는 Admin asset API를 임시로 사용.

### 크리에이터 스튜디오 UI 구성

```
┌──────────────────────────────────────────────────┐
│ 크리에이터 스튜디오                    [채널 보기] │
├──────────┬───────────────────────────────────────┤
│          │                                        │
│ 채널 설정 │  [프로필 이미지]  채널명: ________      │
│          │  [배너 이미지]    설명: ____________     │
│ 내 콘텐츠 │                  [저장]                 │
│          │                                        │
│ 시리즈    │──────────────────────────────────────│
│          │                                        │
│          │  내 콘텐츠                [+ 새 업로드] │
│          │  ┌──────────────────────────────────┐ │
│          │  │ 제목      | 타입  | 상태    | 날짜 │ │
│          │  │ 내 첫 영상 | MOVIE | PUBLISHED | ... │ │
│          │  │ 에피소드1  | EP   | DRAFT    | ... │ │
│          │  └──────────────────────────────────┘ │
│          │                                        │
└──────────┴───────────────────────────────────────┘
```

### 구현 체크리스트

- [ ] 크리에이터 스튜디오 라우트 및 레이아웃
- [ ] 채널 설정 페이지 (이름, 설명, 프로필/배너 이미지 수정)
- [ ] 내 콘텐츠 목록 테이블 (상태별 필터링, 정렬)
- [ ] 콘텐츠 생성 폼 (MOVIE / EPISODE 모드 전환)
- [ ] EPISODE 모드: 시리즈 선택 또는 새 시리즈 생성
- [ ] 콘텐츠 상태 변경 (PUBLISHED ↔ UNLISTED)
- [ ] 콘텐츠 삭제 (확인 다이얼로그)
- [ ] 콘텐츠 메타데이터 수정 (제목, 설명)
- [ ] 시리즈 관리 페이지
- [ ] videoAssetStatus에 따른 상태 표시 (업로드중 / 트랜스코딩중 / 준비됨 / 실패)
- [ ] 비디오 파일 업로드 기능 (기존 asset API 연동)

---

## 4. 기존 페이지에 채널 링크 추가

홈, 카탈로그, 콘텐츠 상세 페이지에서 채널로 이동할 수 있는 링크 추가.

### 콘텐츠 상세 페이지

> **서버 반영 완료:** `GET /api/app/contents/{contentId}` 응답에 `channelHandle`, `channelName` 필드가 이미 포함됨.

```json
// GET /api/app/contents/{contentId} 응답 예시
{
  "id": "uuid",
  "title": "test",
  "status": "READY",
  "channelHandle": "official",
  "channelName": "AI OTT Official",
  "..."
}
```

### 구현 체크리스트

- [ ] 콘텐츠 상세에 채널 링크 표시 (`channelHandle`로 `/channels/{handle}` 이동)
- [ ] 피드 아이템에 채널명 표시 (피드 API에도 채널 정보 추가 필요 — 서버 TODO)

---

## 5. 공통 참고사항

### 5.1 인증 (JWT)

모든 Creator API와 구독 API는 JWT 필수:

```typescript
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};
```

JWT는 기존 로그인 API(`POST /auth/login`)에서 받은 `accessToken` 사용.

### 5.2 다국어 (lang 파라미터)

채널/콘텐츠 이름이 다국어로 저장될 수 있음. 항상 `lang` 파라미터 전달 권장:

```typescript
// Accept-Language 헤더 또는 쿼리 파라미터
GET /api/app/channels/official?lang=ko

// 우선순위: Accept-Language 헤더 > lang 쿼리 > 기본값 'en'
```

### 5.3 에러 응답 형식

```typescript
// 400 Bad Request
{ "error": "IllegalArgumentException", "message": "Channel not found: unknown-handle" }

// 403 Forbidden (소유권 검증 실패)
{ "error": "SecurityException", "message": "Not the owner of this content" }

// 401 Unauthorized (JWT 없거나 만료)
{ "error": "SecurityException", "message": "Not authenticated" }
```

### 5.4 Content Status 값

| Status | 의미 | 시청자 노출 |
|--------|------|------------|
| `DRAFT` | 작성 중 | X |
| `PUBLISHED` | 공개됨 | O |
| `UNLISTED` | 비공개 (URL 직접 접근만 가능) | 제한적 |
| `ARCHIVED` | 삭제됨 (소프트 삭제) | X |

### 5.5 Video Asset Status 값

| Status | 의미 | UI 표시 |
|--------|------|---------|
| `null` | 비디오 미첨부 | "비디오 업로드 필요" |
| `UPLOADED` | 업로드 완료, 트랜스코딩 대기 | 스피너 |
| `TRANSCODING` | 트랜스코딩 진행 중 | 프로그레스 바 |
| `READY` | 재생 가능 | 재생 아이콘 |
| `FAILED` | 트랜스코딩 실패 | 에러 아이콘 + 재시도 버튼 |

### 5.6 이미지 URL 처리

현재 프로필/배너 이미지는 URL 문자열만 저장. 이미지 업로드 자체는 별도 처리 필요:

**방법 A (권장):** 클라이언트에서 S3/R2에 직접 업로드 후 URL을 서버에 전달
```typescript
// 1. 이미지를 S3에 업로드 (presigned URL 또는 클라이언트 직접)
const imageUrl = await uploadToS3(file);

// 2. 채널 정보 업데이트
PUT /api/app/creator/channel
{ "name": "채널명", "profileImageUrl": imageUrl }
```

**방법 B:** 서버에 multipart 업로드 API 추가 (현재 미구현)

---

## 6. 서버 작업 상태 (웹 관련)

| # | 내용 | 상태 |
|---|------|------|
| ~~1~~ | ~~ContentViewResult에 channelHandle, channelName 추가~~ | ✅ 완료 |
| 2 | 크리에이터 전용 비디오 업로드 API (`POST /api/app/creator/contents/{id}/upload`) | ⏳ TODO |
| 3 | 피드 API 응답에 채널 정보 추가 | ⏳ TODO |
| 4 | 채널 handle 변경 API | 낮음 |
| 5 | 프로필/배너 이미지 업로드 API (또는 presigned URL) | 낮음 |
| 6 | 구독 여부 확인 단일 API (`GET /api/app/channels/{handle}/subscription-status`) | 낮음 |
