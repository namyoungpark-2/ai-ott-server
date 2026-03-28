# Creator & Channel System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모든 사용자가 채널을 통해 콘텐츠를 업로드/관리할 수 있는 UGC 시스템 구축 (YouTube+Netflix 하이브리드)

**Architecture:** Hexagonal architecture 패턴 유지. channel 테이블 신규 생성, content/series에 channel_id FK 추가. Creator API(/api/app/creator), Channel Public API(/api/app/channels), Admin Channel API(/api/admin/channels) 3개 레이어.

**Tech Stack:** Spring Boot 4.0.2, JPA + Native SQL, PostgreSQL, Flyway, JWT Auth

**Status:** 전체 Task 1~8 완료 (100%), 커밋 대기

---

## File Structure

### New Files
- `src/main/resources/db/migration/V16__channel_system.sql` — DB 마이그레이션
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/entity/ChannelJpaEntity.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/entity/ChannelI18nJpaEntity.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/entity/ChannelSubscriptionJpaEntity.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/repository/ChannelJpaRepository.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/repository/ChannelSubscriptionJpaRepository.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/adapter/ChannelPersistenceAdapter.java`
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/adapter/ChannelSubscriptionPersistenceAdapter.java`
- `src/main/java/com/aiott/ottpoc/application/port/out/ChannelCommandPort.java`
- `src/main/java/com/aiott/ottpoc/application/port/out/ChannelQueryPort.java`
- `src/main/java/com/aiott/ottpoc/application/port/out/ChannelSubscriptionPort.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/CreatorChannelUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/CreatorContentUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/CreatorSeriesUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/ChannelPublicUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/ChannelSubscriptionUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/port/in/AdminChannelUseCase.java`
- `src/main/java/com/aiott/ottpoc/application/service/CreatorChannelService.java`
- `src/main/java/com/aiott/ottpoc/application/service/CreatorContentService.java`
- `src/main/java/com/aiott/ottpoc/application/service/CreatorSeriesService.java`
- `src/main/java/com/aiott/ottpoc/application/service/ChannelPublicService.java`
- `src/main/java/com/aiott/ottpoc/application/service/ChannelSubscriptionService.java`
- `src/main/java/com/aiott/ottpoc/application/service/AdminChannelService.java`
- `src/main/java/com/aiott/ottpoc/application/dto/channel/` — Channel DTOs (11 records)
- `src/main/java/com/aiott/ottpoc/adapter/in/web/app/CreatorChannelController.java`
- `src/main/java/com/aiott/ottpoc/adapter/in/web/app/CreatorContentController.java`
- `src/main/java/com/aiott/ottpoc/adapter/in/web/app/CreatorSeriesController.java`
- `src/main/java/com/aiott/ottpoc/adapter/in/web/app/ChannelPublicController.java`
- `src/main/java/com/aiott/ottpoc/adapter/in/web/app/ChannelSubscriptionController.java`
- `src/main/java/com/aiott/ottpoc/adapter/in/web/admin/AdminChannelController.java`

### Modified Files
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/entity/ContentJpaEntity.java` — channel FK 추가
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/entity/SeriesJpaEntity.java` — channel FK 추가
- `src/main/java/com/aiott/ottpoc/adapter/out/persistence/jpa/adapter/CatalogCommandAdapter.java` — channel_id 파라미터 추가
- `src/main/java/com/aiott/ottpoc/application/port/out/CatalogCommandPort.java` — channel_id 파라미터 추가

---

### Task 1: DB Migration (V16) ✅ COMPLETE

**Files:**
- Create: `src/main/resources/db/migration/V16__channel_system.sql`

- [x] **Step 1: Write V16 migration SQL** — channel, channel_i18n, channel_subscription 테이블 생성. content/series에 channel_id 추가. 공식 채널 시드. 기존 데이터 마이그레이션.
- [x] **Step 2: Verify migration runs** — 부팅 성공, V16 마이그레이션 적용 확인, 공식 채널 생성 확인
- [ ] **Step 3: Commit** — 전체 작업 완료 후 일괄 커밋 예정

---

### Task 2: Channel JPA Entities + Repositories ✅ COMPLETE

**Files:**
- Create: `ChannelJpaEntity.java`, `ChannelI18nJpaEntity.java`, `ChannelSubscriptionJpaEntity.java`
- Create: `ChannelJpaRepository.java`, `ChannelSubscriptionJpaRepository.java`
- Modify: `ContentJpaEntity.java`, `SeriesJpaEntity.java`

- [x] **Step 1: Create Channel entities following existing patterns**
- [x] **Step 2: Add channel FK to Content and Series entities**
- [x] **Step 3: Create repositories**
- [x] **Step 4: Compile check** — BUILD SUCCESSFUL
- [ ] **Step 5: Commit** — 전체 작업 완료 후 일괄 커밋 예정

---

### Task 3: Channel Ports (Interfaces) ✅ COMPLETE

**Files:**
- Create: `ChannelCommandPort.java`, `ChannelQueryPort.java`, `ChannelSubscriptionPort.java`
- Create: `CreatorChannelUseCase.java`, `CreatorContentUseCase.java`, `CreatorSeriesUseCase.java`
- Create: `ChannelPublicUseCase.java`, `ChannelSubscriptionUseCase.java`, `AdminChannelUseCase.java`
- Modify: `CatalogCommandPort.java`

- [x] **Step 1: Create out ports** — 3개 port interface
- [x] **Step 2: Create in ports (use cases)** — 6개 use case interface
- [x] **Step 3: Update CatalogCommandPort to accept channelId** — `createMovieContentWithChannel`, `createSeriesWithChannel` 추가
- [ ] **Step 4: Commit** — 전체 작업 완료 후 일괄 커밋 예정

---

### Task 4: Channel DTOs ✅ COMPLETE

**Files:**
- Create: `src/main/java/com/aiott/ottpoc/application/dto/channel/*.java`

- [x] **Step 1: Create all channel DTOs as records** — 11개 record 생성 (ChannelDetailResult, ChannelSummaryResult, ChannelContentResult, ChannelSeriesResult, UpdateChannelCommand, CreatorCreateContentCommand, CreatorContentResult, CreatorContentSummary, CreatorCreateSeriesCommand, CreatorSeriesResult)
- [ ] **Step 2: Commit** — 전체 작업 완료 후 일괄 커밋 예정

---

### Task 5: Channel Persistence Adapters ✅ COMPLETE

**Files:**
- Create: `ChannelPersistenceAdapter.java`, `ChannelSubscriptionPersistenceAdapter.java`
- Modify: `CatalogCommandAdapter.java`

- [x] **Step 1: Implement ChannelPersistenceAdapter (native SQL)** — ChannelCommandPort + ChannelQueryPort 구현
- [x] **Step 2: Implement ChannelSubscriptionPersistenceAdapter** — subscribe/unsubscribe/isSubscribed/listSubscriptions
- [x] **Step 3: Update CatalogCommandAdapter for channel_id** — createMovieContentWithChannel, createSeriesWithChannel 구현
- [x] **Step 4: Compile check** — BUILD SUCCESSFUL
- [ ] **Step 5: Commit** — 전체 작업 완료 후 일괄 커밋 예정

**Post-fix:** Hibernate 7의 TIMESTAMPTZ→Instant 반환 이슈 수정 (java.sql.Timestamp → Instant/OffsetDateTime 호환 처리)

---

### Task 6: Channel Services ✅ COMPLETE

**Files:**
- Create: all 6 service files

- [x] **Step 1: Implement CreatorChannelService (auto-create channel on first access)**
- [x] **Step 2: Implement CreatorContentService (upload with channel ownership)**
- [x] **Step 3: Implement CreatorSeriesService**
- [x] **Step 4: Implement ChannelPublicService**
- [x] **Step 5: Implement ChannelSubscriptionService**
- [x] **Step 6: Implement AdminChannelService**
- [ ] **Step 7: Commit** — 전체 작업 완료 후 일괄 커밋 예정

**Post-fix:** CreatorContentService에 OffsetDateTime import 누락 수정

---

### Task 7: Controllers ✅ COMPLETE

**Files:**
- Create: all 6 controller files

- [x] **Step 1: CreatorChannelController**
- [x] **Step 2: CreatorContentController**
- [x] **Step 3: CreatorSeriesController**
- [x] **Step 4: ChannelPublicController**
- [x] **Step 5: ChannelSubscriptionController**
- [x] **Step 6: AdminChannelController**
- [x] **Step 7: Compile and boot check** — 부팅 성공, API 응답 확인
- [ ] **Step 8: Commit** — 전체 작업 완료 후 일괄 커밋 예정

**검증 완료:**
- `GET /api/app/channels/official` → 200 OK (채널 상세)
- `GET /api/app/channels/official/contents` → 200 OK (4개 콘텐츠)
- `GET /api/admin/channels` → 200 OK (공식 채널 1개)

---

### Task 8: Integration — Update Existing Upload Flow ✅ COMPLETE

**Files:**
- Modify: `UnifiedUploadService.java` — channel_id 전달
- Modify: `AdminContentService.java` — 공식 채널 사용
- Modify: `ContentViewResult.java` — channelHandle, channelName 추가
- Modify: `ContentViewQueryAdapter.java` — channel JOIN 추가
- Modify: `AdminContentSummary.java` — channelHandle, channelName 추가
- Modify: `AdminContentQueryAdapter.java` — channel JOIN 추가

- [x] **Step 1: Update UnifiedUploadService** — ChannelQueryPort 주입, createMovieContentWithChannel/createSeriesWithChannel 사용
- [x] **Step 2: Update AdminContentService** — ChannelQueryPort 주입, 공식 채널 ID 조회 후 channel-aware 메서드 사용
- [x] **Step 3: ContentViewResult에 channelHandle, channelName 추가** — 콘텐츠 상세 API에 채널 정보 포함
- [x] **Step 4: AdminContentSummary에 channelHandle, channelName 추가** — 어드민 콘텐츠 목록에 채널 정보 포함
- [x] **Step 5: Boot and verify** — 모든 API 정상 동작 확인
- [ ] **Step 6: Commit** — 전체 작업 완료 후 일괄 커밋 예정

---

## Progress Summary

| Task | Status | Description |
|------|--------|-------------|
| Task 1: DB Migration | ✅ | V16 channel_system.sql 적용 완료 |
| Task 2: JPA Entities | ✅ | 3 new entities + 2 modified + 2 repos |
| Task 3: Ports | ✅ | 3 out ports + 6 use cases + CatalogCommandPort 수정 |
| Task 4: DTOs | ✅ | 11 record classes |
| Task 5: Adapters | ✅ | 2 new adapters + CatalogCommandAdapter 수정 |
| Task 6: Services | ✅ | 6 service implementations |
| Task 7: Controllers | ✅ | 6 controllers, API 검증 완료 |
| Task 8: Integration | ✅ | Admin 업로드 통합 + ContentView/AdminSummary에 channel 정보 추가 |

**Overall: 8/8 Tasks Complete (100%)** — 커밋만 남음
