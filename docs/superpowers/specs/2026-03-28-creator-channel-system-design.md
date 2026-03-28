# Creator & Channel System Design

> YouTube-style UGC + Netflix-style Curation Hybrid Architecture

## Summary

기존 Admin 전용 콘텐츠 관리 시스템을 모든 사용자가 콘텐츠를 업로드하고 채널을 통해 관리할 수 있는 UGC 플랫폼으로 전환한다. 넷플릭스형 큐레이션 카탈로그와 유튜브형 채널 페이지가 공존하는 하이브리드 구조.

## Decisions

| 항목 | 결정 |
|------|------|
| 업로드 권한 | 초기 오픈 UGC, 향후 승인제/티어제 전환 가능 |
| 크리에이터 관리 범위 | 업로드 + 메타데이터 + 시리즈 관리 (수익화/티어는 운영진) |
| 공개 프로세스 | 초기 즉시공개, 향후 PENDING_REVIEW 검수 단계 추가 가능 |
| 채널 | 프로필 + 배너 + 구독/구독자수 (플레이리스트/커뮤니티는 확장 가능 설계) |
| 기존 콘텐츠 | 공식 채널로 마이그레이션, 단일 모델 |
| 홈/카탈로그 | 채널 무관 큐레이션(넷플릭스) + 채널 페이지(유튜브) 공존 |

## Data Model

### New Tables

#### channel
| Column | Type | Constraint |
|--------|------|------------|
| id | UUID | PK |
| owner_id | UUID | FK → users, UNIQUE, nullable (공식채널은 NULL) |
| handle | VARCHAR(50) | UNIQUE, NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| description | TEXT | |
| profile_image_url | VARCHAR(500) | |
| banner_image_url | VARCHAR(500) | |
| is_official | BOOLEAN | DEFAULT false |
| subscriber_count | INT | DEFAULT 0 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' (ACTIVE, SUSPENDED) |
| created_at | TIMESTAMPTZ | DEFAULT now() |
| updated_at | TIMESTAMPTZ | DEFAULT now() |

#### channel_i18n
| Column | Type | Constraint |
|--------|------|------------|
| id | BIGSERIAL | PK |
| channel_id | UUID | FK → channel |
| lang | VARCHAR(10) | NOT NULL |
| name | VARCHAR(100) | |
| description | TEXT | |

UNIQUE(channel_id, lang)

#### channel_subscription
| Column | Type | Constraint |
|--------|------|------------|
| subscriber_id | UUID | PK, FK → users |
| channel_id | UUID | PK, FK → channel |
| created_at | TIMESTAMPTZ | DEFAULT now() |

### Modified Tables

#### content
- ADD `channel_id UUID FK → channel NOT NULL`

#### series
- ADD `channel_id UUID FK → channel NOT NULL`

## API Design

### Creator APIs — /api/app/creator (User JWT 필수)

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/app/creator/channel | 내 채널 정보 조회 (없으면 자동 생성) |
| PUT | /api/app/creator/channel | 채널 정보 수정 |
| POST | /api/app/creator/contents | 콘텐츠 업로드 (multipart) |
| GET | /api/app/creator/contents | 내 콘텐츠 목록 |
| PUT | /api/app/creator/contents/{id}/metadata | 메타데이터 수정 |
| PATCH | /api/app/creator/contents/{id}/status | 공개/비공개 전환 |
| DELETE | /api/app/creator/contents/{id} | 콘텐츠 삭제 |
| POST | /api/app/creator/series | 시리즈 생성 |
| GET | /api/app/creator/series | 내 시리즈 목록 |
| PUT | /api/app/creator/series/{id} | 시리즈 수정 |

### Channel Public APIs — /api/app/channels

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/app/channels/{handle} | Public | 채널 페이지 조회 |
| GET | /api/app/channels/{handle}/contents | Public | 채널 콘텐츠 목록 |
| GET | /api/app/channels/{handle}/series | Public | 채널 시리즈 목록 |
| POST | /api/app/channels/{handle}/subscribe | JWT | 채널 구독 |
| DELETE | /api/app/channels/{handle}/subscribe | JWT | 구독 해제 |
| GET | /api/app/me/subscriptions | JWT | 내 구독 채널 목록 |

### Admin Channel APIs — /api/admin/channels

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/admin/channels | 전체 채널 목록 |
| PATCH | /api/admin/channels/{id}/status | 채널 정지/활성화 |

## Architecture (Hexagonal)

### IN (Web Adapters)
- CreatorContentController
- CreatorChannelController
- CreatorSeriesController
- ChannelPublicController
- ChannelSubscriptionController
- AdminChannelController

### APPLICATION (Use Cases)
- CreatorUploadService — 크리에이터 콘텐츠 업로드 (채널 자동생성 + 기존 UnifiedUploadService 재사용)
- CreatorContentService — 크리에이터 콘텐츠 CRUD
- CreatorSeriesService — 크리에이터 시리즈 CRUD
- ChannelService — 채널 조회/수정
- ChannelSubscriptionService — 구독/해제, 구독자수 관리
- AdminChannelService — 관리자 채널 관리

### OUT (Persistence)
- ChannelJpaEntity, ChannelI18nJpaEntity, ChannelSubscriptionJpaEntity
- ChannelJpaRepository, ChannelSubscriptionJpaRepository
- ChannelPersistenceAdapter

## Migration Strategy (V16)

1. channel, channel_i18n, channel_subscription 테이블 생성
2. content.channel_id, series.channel_id 컬럼 추가 (nullable)
3. "AI OTT Official" 시스템 채널 INSERT (is_official=true, owner_id=NULL)
4. 기존 content/series의 channel_id를 공식 채널 ID로 UPDATE
5. channel_id에 NOT NULL 제약 추가
6. 인덱스: channel.handle, channel.owner_id, content.channel_id, series.channel_id

## Ownership & Authorization Rules

- 크리에이터는 자기 채널의 콘텐츠/시리즈만 수정/삭제 가능
- content.channel_id → channel.owner_id로 소유권 검증
- Admin은 모든 채널/콘텐츠 관리 가능
- 수익화(required_tier) 변경은 Admin만 가능

## Future Extensibility (설계만)

- **Playlist/Collection**: channel_playlist, playlist_item 테이블
- **Community Tab**: channel_post 테이블
- **Content Review Gate**: PENDING_REVIEW 상태 활성화
- **Creator Approval**: channel.status 기반 업로드 권한 게이팅
- **Revenue Share**: creator_revenue 테이블, 정산 시스템
