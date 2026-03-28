# Creator & Channel System Architecture

## Why

기존 시스템은 Admin만 콘텐츠를 관리할 수 있는 넷플릭스형 구조였다.
플랫폼 성장을 위해 모든 사용자가 콘텐츠를 업로드하고 채널을 통해 관리할 수 있는 UGC 시스템이 필요했다.
유튜브형 채널 페이지와 넷플릭스형 큐레이션 카탈로그가 공존하는 하이브리드 구조를 선택했다.

## AS-IS → TO-BE

### AS-IS
- Admin만 콘텐츠 업로드/관리
- 콘텐츠에 소유자 개념 없음
- User는 시청자 역할만
- 단일 카탈로그 (넷플릭스형)

### TO-BE
- 모든 User가 업로드 가능 (오픈 UGC)
- 콘텐츠 → 채널 소속 (channel_id FK)
- User → 채널 소유자 = 크리에이터
- 카탈로그(넷플릭스) + 채널 페이지(유튜브) 공존
- 채널 구독 시스템
- 기존 콘텐츠 → "AI OTT Official" 공식 채널로 마이그레이션

## 전체 흐름

### 크리에이터 콘텐츠 업로드

```
User JWT → CreatorContentController
  → CreatorContentService
    → ChannelQueryPort (채널 조회/자동생성)
    → CatalogCommandPort.createMovieContentWithChannel (콘텐츠 + channel_id)
    → updateContentStatus("PUBLISHED")  // 초기: 즉시 공개
  → Response: { contentId }
```

### 채널 페이지 조회 (Public)

```
GET /api/app/channels/{handle}
  → ChannelPublicService
    → ChannelQueryPort.findByHandle (채널 + i18n)
  → Response: ChannelDetailResult

GET /api/app/channels/{handle}/contents
  → ChannelQueryPort.listContentsByChannelHandle (PUBLISHED만)
  → Response: [ChannelContentResult]
```

### 홈/카탈로그 (기존 — 변경 없음)

```
GET /api/app/feed       → 기존 FeedController (채널 무관)
GET /api/app/catalog/*  → 기존 CatalogController (채널 무관)
```

기존 피드/카탈로그는 content 테이블을 channel_id 무관하게 조회하므로
모든 채널의 PUBLISHED 콘텐츠가 자동으로 노출된다.

### 채널 구독

```
POST /api/app/channels/{handle}/subscribe
  → ChannelSubscriptionService
    → ChannelSubscriptionPort.subscribe (INSERT channel_subscription)
    → ChannelCommandPort.incrementSubscriberCount
```

## 파일 매핑

### 신규 생성
| Layer | Files |
|-------|-------|
| Migration | `V16__channel_system.sql` |
| Entity | `ChannelJpaEntity`, `ChannelI18nJpaEntity`, `ChannelSubscriptionJpaEntity` |
| Repository | `ChannelJpaRepository`, `ChannelSubscriptionJpaRepository` |
| Port (out) | `ChannelCommandPort`, `ChannelQueryPort`, `ChannelSubscriptionPort` |
| Port (in) | `CreatorChannelUseCase`, `CreatorContentUseCase`, `CreatorSeriesUseCase`, `ChannelPublicUseCase`, `ChannelSubscriptionUseCase`, `AdminChannelUseCase` |
| Service | `CreatorChannelService`, `CreatorContentService`, `CreatorSeriesService`, `ChannelPublicService`, `ChannelSubscriptionService`, `AdminChannelService` |
| Controller | `CreatorChannelController`, `CreatorContentController`, `CreatorSeriesController`, `ChannelPublicController`, `ChannelSubscriptionController`, `AdminChannelController` |
| DTO | `channel/` 패키지 — 11개 record |
| Adapter | `ChannelPersistenceAdapter`, `ChannelSubscriptionPersistenceAdapter` |

### 수정
| File | Change |
|------|--------|
| `ContentJpaEntity` | channel FK 추가 |
| `SeriesJpaEntity` | channel FK 추가 |
| `CatalogCommandPort` | `createMovieContentWithChannel`, `createSeriesWithChannel` 메서드 추가 |
| `CatalogCommandAdapter` | 위 메서드 구현 |

## 소유권 검증

크리에이터가 콘텐츠/시리즈를 수정/삭제할 때:
```
content.channel_id → channel.owner_id == JWT subject
```
`CreatorContentService.verifyOwnership()`에서 검증.

## 향후 확장 포인트

| Feature | 접근 방식 |
|---------|-----------|
| 검수 시스템 | content.status에 PENDING_REVIEW 추가, 트랜스코딩 완료 후 PUBLISHED 대신 PENDING_REVIEW로 설정 |
| 크리에이터 승인제 | channel.status 기반 게이팅, SUSPENDED 채널은 업로드 불가 |
| 구독 티어 제한 | 업로드 시 user.subscription_tier 확인 로직 추가 |
| 플레이리스트 | channel_playlist, playlist_item 테이블 추가 |
| 커뮤니티 | channel_post 테이블 추가 |

## 사이드 이펙트

- 기존 Admin 업로드 API는 그대로 동작 (공식 채널로 귀속)
- 기존 피드/카탈로그/검색 API는 변경 없음 — channel_id와 무관하게 전체 PUBLISHED 콘텐츠 대상
- user_watch_progress, watch_event 등 시청 관련 기능에 영향 없음

## 검증 방법

```bash
# 서버 시작
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
./gradlew bootRun

# 공식 채널 조회
curl http://localhost:8080/api/app/channels/official

# 공식 채널 콘텐츠 목록
curl http://localhost:8080/api/app/channels/official/contents

# 관리자 채널 목록
curl http://localhost:8080/api/admin/channels

# 크리에이터 채널 (JWT 필요)
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/app/creator/channel
```
