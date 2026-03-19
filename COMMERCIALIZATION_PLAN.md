# OTT Platform — Commercialization & Production Readiness Analysis

> **Purpose**: A complete, plain-language breakdown of where the platform stands today, what's missing before it can be launched to real paying users, and how the architecture needs to evolve.
> **Audience**: Anyone — technical or not.

---

## Table of Contents

1. [What Is This Platform? (The Big Picture)](#1-what-is-this-platform)
2. [Current Architecture — How Everything Fits Together Today](#2-current-architecture)
3. [What's Already Built (The Green Checkmarks)](#3-whats-already-built)
4. [What's Missing Before Real Users Can Pay (The Red X's)](#4-whats-missing)
5. [Future Architecture — What It Must Become](#5-future-architecture)
6. [Prioritized Roadmap](#6-prioritized-roadmap)

---

## 1. What Is This Platform?

Imagine Netflix — but you own it. Users visit a website, sign up, browse TV series and episodes, click play, and watch streaming video. You (the admin) upload the video content, organize it into shows, and eventually charge users a monthly fee to access it.

This platform has three separate "apps" that work together:

| App | Who Uses It | What It Does |
|-----|-------------|--------------|
| **ai-ott-server** | Nobody sees it directly — it's the brain | Stores all data, handles logins, streams video, enforces rules |
| **ai-ott-web** | Your paying customers | Browse, watch, manage their account |
| **ai-ott-admin** | You (the content manager) | Upload videos, create shows, manage content |

Think of a restaurant: the **server** is the kitchen, the **web app** is the dining room for guests, and the **admin** is the manager's back office.

---

## 2. Current Architecture

### How the Pieces Talk to Each Other (Today)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLOUDFLARE (Global CDN)                      │
│                                                                       │
│   ┌─────────────────────────┐    ┌───────────────────────────────┐  │
│   │  ai-ott.pages.dev       │    │  admin.ai-ott.pages.dev       │  │
│   │  USER WEB APP           │    │  ADMIN PANEL                  │  │
│   │  (Next.js 16 / React)   │    │  (Next.js 16 / React)         │  │
│   │  • Browse series        │    │  • Upload videos              │  │
│   │  • Watch video (HLS)    │    │  • Create/edit series         │  │
│   │  • Watchlist/history    │    │  • Manage episodes            │  │
│   └────────────┬────────────┘    └──────────────┬────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                 │  HTTPS API calls                │
                 ▼                                 ▼
┌────────────────────────────────────────────────────────────────────┐
│              ai-ott-api.onrender.com  (RENDER cloud)               │
│              BACKEND SERVER (Spring Boot 4 / Java)                  │
│                                                                      │
│  • REST API endpoints (/api/auth, /api/app, /api/admin)             │
│  • JWT authentication (login tokens, refresh tokens)                │
│  • Video upload & FFmpeg transcoding → HLS format                   │
│  • Role-based access (ADMIN vs. USER)                               │
└──────────────┬──────────────────────────┬───────────────────────────┘
               │                          │
               ▼                          ▼
┌──────────────────────────┐   ┌─────────────────────────────────────┐
│  PostgreSQL Database     │   │  Cloudflare R2 (Object Storage)     │
│  (hosted on Render)      │   │  (like Amazon S3, but cheaper)      │
│                          │   │                                     │
│  Tables:                 │   │  Stores:                            │
│  • users                 │   │  • .mp4 original video files        │
│  • subscriptions         │   │  • .ts  HLS video segments          │
│  • series                │   │  • .m3u8 HLS playlists              │
│  • episodes              │   │  • .jpg thumbnail images            │
│  • watchlist             │   │                                     │
│  • watch_history         │   │                                     │
└──────────────────────────┘   └─────────────────────────────────────┘
```

### What Technologies Are Being Used

**Backend (ai-ott-server)**
- **Java 26 + Spring Boot 4** — like the steel frame of a skyscraper; handles all requests
- **PostgreSQL** — the filing cabinet; stores users, shows, episodes
- **Flyway** — keeps database changes organized (like version control for your database)
- **JWT tokens** — digital ID cards users carry to prove who they are
- **FFmpeg** — the video converter; takes raw .mp4 uploads and breaks them into streamable HLS chunks
- **Cloudflare R2** — the video warehouse; stores all video files cheaply
- **Hexagonal Architecture** — a clean code design that keeps business rules separate from storage/network details, making it easy to change one without breaking the other

**Frontend (ai-ott-web & ai-ott-admin)**
- **Next.js 16 (React)** — the engine that builds the web pages users see
- **TypeScript** — JavaScript with stricter rules, catches bugs before they happen
- **TailwindCSS + shadcn/ui** — styling tools that make the pages look good
- **HLS.js** — the video player that handles streaming
- **Zod + React Hook Form** — validates form inputs so bad data can't be submitted
- **Axios** — the messenger that carries requests between frontend and backend

---

## 3. What's Already Built

### Backend ✅

| Feature | Status | Details |
|---------|--------|---------|
| User registration & login | ✅ Done | Email + password, bcrypt hashing |
| JWT auth (access + refresh tokens) | ✅ Done | 24h access, 7-day refresh |
| Role system (ADMIN vs USER) | ✅ Done | Different permission levels enforced |
| Series & Episode data model | ✅ Done | Full CRUD (create, read, update, delete) |
| Video upload endpoint | ✅ Done | Multipart file upload to server |
| FFmpeg HLS transcoding | ✅ Done | Converts MP4 → HLS segments |
| Thumbnail generation | ✅ Done | Auto-extracts frame from video |
| Upload status tracking | ✅ Done | Polling endpoint for transcode progress |
| Cloudflare R2 storage integration | ✅ Done | S3-compatible, environment-toggled |
| Watchlist system | ✅ Done | Add/remove episodes per user |
| Watch history + progress | ✅ Done | Saves progress, allows resume |
| Database migrations (Flyway) | ✅ Done | Versioned schema changes |
| CORS configuration | ✅ Done | Only allows known frontend origins |
| Docker deployment | ✅ Done | Containerized for Render |
| Health check endpoint | ✅ Done | /api/ops/health |
| Subscription model in DB | ✅ Done | FREE / BASIC / PREMIUM plans exist |

### Admin Panel ✅

| Feature | Status |
|---------|--------|
| Admin login (role-gated) | ✅ Done |
| Series list table | ✅ Done |
| Create / Edit / Delete series | ✅ Done |
| Episode list per series | ✅ Done |
| Create / Edit / Delete episodes | ✅ Done |
| Video file upload with progress bar | ✅ Done |
| Transcode status polling (upload → queued → transcoding → done) | ✅ Done |
| Form validation (Zod schemas) | ✅ Done |
| Route protection (non-admins blocked) | ✅ Done |

### User Web App ✅

| Feature | Status |
|---------|--------|
| Register & Login pages | ✅ Done |
| Browse all series (grid view) | ✅ Done |
| Series detail page | ✅ Done |
| Episode list per series | ✅ Done |
| HLS video player | ✅ Done |
| Watchlist (add/remove/view) | ✅ Done |
| Watch history | ✅ Done |
| User profile page | ✅ Done |
| Responsive design (mobile) | ✅ Done |
| Protected routes (must be logged in) | ✅ Done |

---

## 4. What's Missing Before Real Users Can Pay

This is the most important section. Think of launching a store: you've built the shelves and stocked the products, but you haven't installed a cash register, locked the back door, or gotten a business license. Here's everything that needs to be done, grouped by priority.

---

### 🔴 CRITICAL — Platform Cannot Launch Without These

#### 4.1 Payment System (Stripe Integration)

**What's missing**: There is zero payment processing. The database has a `subscription` table with FREE/BASIC/PREMIUM plans, but there's no way for a user to actually *pay* to upgrade. No Stripe, no PayPal, nothing.

**What this means for a normal person**: Imagine a vending machine with no coin slot. You can see the snacks but can never buy them.

**What needs to be built**:
- **Backend**:
  - Integrate Stripe SDK (Java)
  - `POST /api/payments/create-checkout-session` — redirect user to Stripe's hosted payment page
  - `POST /api/payments/webhook` — Stripe calls this endpoint when payment succeeds/fails/renews
  - Webhook signature verification (so nobody can fake a payment)
  - Subscription lifecycle handlers: created, renewed, cancelled, payment failed
  - Link Stripe customer ID to user record in database
  - Update `subscriptions` table when Stripe events arrive
- **Frontend**:
  - "Upgrade to Premium" button/page on user web app
  - Pricing page (show FREE vs BASIC vs PREMIUM features and prices)
  - Subscription management page (cancel, view renewal date)
  - Post-payment success/failure redirect pages
  - Show paywall when FREE user tries to access premium content

#### 4.2 Subscription Enforcement (Content Gating)

**What's missing**: Even if you had payments, nothing stops a FREE user from watching premium content. The `subscription` field exists in the DB but no API endpoint actually checks it before streaming video.

**What needs to be built**:
- Backend middleware that checks subscription tier before serving episode content
- "Access denied" response (HTTP 403) for users trying to watch above their plan
- Episode metadata (mark which episodes/series require BASIC or PREMIUM)
- Admin ability to set content tier when creating episodes
- Frontend: lock icons / blurred thumbnails on premium content for free users

#### 4.3 Email Verification & Password Reset

**What's missing**: Users can register with any email (real or fake) and there's no way to reset a forgotten password. This is a showstopper for real users.

**What needs to be built**:
- **Backend**:
  - Email sending service (SendGrid, Postmark, or AWS SES)
  - Verification token generation and storage
  - `POST /api/auth/verify-email` endpoint
  - `POST /api/auth/forgot-password` — sends reset email
  - `POST /api/auth/reset-password` — validates token, updates password
  - Block unverified users from accessing content
- **Frontend**:
  - "Check your email" confirmation page after registration
  - Email verification success/failure page
  - Forgot password form
  - Reset password form (from email link)

#### 4.4 HTTPS / Security Hardening

**What's missing / needs review**:
- JWT secret is a configurable env var — needs to be a strong, cryptographically random 256-bit value in production
- Token stored in `localStorage` on frontend — this is a security risk (XSS can steal it). Should migrate to `httpOnly` cookies only.
- Rate limiting: right now, anyone can hammer the `/api/auth/login` endpoint with millions of password guesses. Need rate limiting (e.g., 5 attempts per minute per IP).
- No account lockout after failed login attempts
- Render free tier spins down after 15 minutes of inactivity — first request takes 30+ seconds. Not acceptable for paying users; needs paid Render plan or alternative.

#### 4.5 Video Content Protection

**What's missing**: HLS video URLs in Cloudflare R2 are probably public (anyone with the URL can stream for free). Real platforms use signed URLs.

**What needs to be built**:
- Time-limited signed URLs for video segments (Cloudflare R2 supports this)
- URLs expire after X minutes so sharing the link doesn't bypass the paywall
- Backend generates signed URL on each valid play request
- Frontend requests play URL from backend, not directly from R2

---

### 🟠 IMPORTANT — Needed Soon After Launch

#### 4.6 Multiple Video Quality Levels (Adaptive Bitrate)

**What's missing**: Currently, FFmpeg creates a single HLS stream. Real streaming platforms (Netflix, YouTube) create multiple quality levels (360p, 480p, 720p, 1080p) so the player automatically picks the best quality based on the viewer's internet speed.

**What needs to be built**:
- Update FFmpeg transcoding job to generate 3-4 quality variants
- Generate a "master" HLS playlist that lists all variants
- HLS.js player on frontend already supports this — just needs the master playlist
- Admin panel should show transcoding status for each quality level

#### 4.7 Search

**What's missing**: With 10+ series, users need to search. Currently there's no search functionality anywhere.

**What needs to be built**:
- Backend: `GET /api/app/series?q=searchterm` full-text search endpoint
- PostgreSQL full-text search (built-in, no extra service needed for V1)
- Frontend: search bar in navbar, search results page

#### 4.8 Error Monitoring & Logging

**What's missing**: If the backend crashes or throws an error in production, you have no way of knowing unless a user complains.

**What needs to be built**:
- Integrate Sentry (or similar) on both frontend and backend
- Structured logging on backend (already using Spring Boot logging, just needs configuration)
- Alert when error rate spikes
- Track which episodes fail to load, which uploads fail to transcode

#### 4.9 Admin User Management

**What's missing**: There's no UI in the admin panel to manage users — see who's subscribed, cancel accounts, handle support requests, manually upgrade/downgrade plans.

**What needs to be built**:
- Admin endpoint: `GET /api/admin/users` (list users, filter by plan)
- Admin endpoint: `PUT /api/admin/users/{id}/subscription` (force change plan)
- Admin endpoint: `DELETE /api/admin/users/{id}` (delete account)
- Admin panel user management page with table, search, and actions

#### 4.10 Terms of Service, Privacy Policy, Cookie Notice

**What's missing**: Legally required in most countries. Without these you cannot collect payment information.

**What needs to be built**:
- Static pages: /terms, /privacy, /cookies
- Checkbox on registration form: "I agree to Terms of Service"
- Cookie consent banner (required for EU users under GDPR)
- Store consent timestamp in database

---

### 🟡 NICE TO HAVE — Post-Launch Improvements

#### 4.11 Video Upload Improvements

- **Poster image upload**: Currently, series poster URLs are typed in manually (a URL string). Should be a file upload with preview.
- **Resumable uploads**: For large video files, if the upload drops halfway, users have to start over. Should implement chunked/resumable uploads (tus protocol or S3 multipart).
- **Upload queue**: Currently one video processes at a time. With many uploads, need a proper background job queue (e.g., Redis + BullMQ or Spring Batch).

#### 4.12 Subtitles / Captions

- Upload SRT/VTT subtitle files per episode
- Display caption selector in video player
- Required for accessibility compliance (ADA in US, EN 301 549 in EU)

#### 4.13 Continue Watching / Recommendations

- "Continue Watching" section on homepage based on watch history
- "Because you watched X" simple recommendation logic
- "New Episodes" notification for series in watchlist

#### 4.14 Mobile App

- React Native app sharing TypeScript types with web app
- Push notifications for new episodes
- Offline download (requires DRM)

#### 4.15 Analytics Dashboard (Admin)

- Total users, active subscribers, revenue
- Most watched series/episodes
- User drop-off points in episodes (engagement heatmap)
- Churn rate, MRR (monthly recurring revenue)

---

## 5. Future Architecture

As the platform grows, the current single-server setup will hit limits. Here's what it needs to evolve into.

### What Breaks Under Load

Imagine 10,000 users all trying to watch video at 9 PM on a Friday. The current setup:
- **One server on Render** handles ALL requests — it will get overwhelmed
- **FFmpeg transcoding runs on the same server** as the API — a big upload will slow down everyone's browsing
- **No caching** — the same database query runs thousands of times per second

### Target Future Architecture

```
                        ┌─────────────────────────────────┐
                        │   CLOUDFLARE (Global CDN + WAF)  │
                        │   • Caches static assets         │
                        │   • DDoS protection              │
                        │   • Routes traffic               │
                        └────────────────┬────────────────┘
                                         │
                  ┌──────────────────────┼──────────────────────┐
                  │                      │                       │
                  ▼                      ▼                       ▼
        ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
        │  ai-ott-web     │   │  ai-ott-admin   │   │  (future)       │
        │  Cloudflare     │   │  Cloudflare     │   │  Mobile App     │
        │  Pages          │   │  Pages          │   │  (React Native) │
        └────────┬────────┘   └────────┬────────┘   └────────┬────────┘
                 │                     │                      │
                 └─────────────────────┴──────────────────────┘
                                       │ HTTPS
                                       ▼
                        ┌──────────────────────────────┐
                        │   API GATEWAY / LOAD BALANCER │
                        │   (Cloudflare Workers or      │
                        │    AWS API Gateway)            │
                        │   • Rate limiting              │
                        │   • Auth token validation      │
                        │   • Request routing            │
                        └──────────────┬───────────────┘
                                       │
              ┌────────────────────────┼────────────────────────┐
              │                        │                         │
              ▼                        ▼                         ▼
  ┌────────────────────┐  ┌────────────────────┐  ┌──────────────────────┐
  │  API SERVICE       │  │  AUTH SERVICE      │  │  PAYMENT SERVICE     │
  │  (Spring Boot)     │  │  (Spring Boot or   │  │  (Stripe webhook     │
  │  • Series/Episodes │  │   Keycloak/Auth0)  │  │   handler)           │
  │  • Watchlist       │  │  • Login/Register  │  │  • Subscription mgmt │
  │  • Watch history   │  │  • JWT issuance    │  │  • Plan enforcement  │
  │  • Search          │  │  • OAuth2 (Google/ │  │  • Billing events    │
  └──────────┬─────────┘  │   Apple sign-in)   │  └──────────────────────┘
             │            └────────────────────┘
             │
  ┌──────────▼──────────┐
  │  TRANSCODING SERVICE│
  │  (Separate worker)  │
  │  • Background jobs  │
  │  • Redis job queue  │
  │  • FFmpeg workers   │
  │  • Multi-quality    │
  │    HLS generation   │
  └─────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │  PostgreSQL      │  │  Redis           │  │  Cloudflare   │ │
│  │  (Primary DB)    │  │  (Cache + Queue) │  │  R2 Storage   │ │
│  │  • Read replicas │  │  • Session cache │  │  • Videos     │ │
│  │    for scaling   │  │  • API response  │  │  • Thumbnails │ │
│  │  • Backups every │  │    caching       │  │  • Signed     │ │
│  │    hour          │  │  • Transcode job │  │    URLs       │ │
│  │  • Point-in-time │  │    queue         │  │  • CDN cached │ │
│  │    recovery      │  └──────────────────┘  └───────────────┘ │
│  └──────────────────┘                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Changes Explained Simply

| Today | Future | Why |
|-------|--------|-----|
| One server does everything | Separate API, Auth, Transcoding services | If transcoding crashes, browsing still works |
| FFmpeg runs on the API server | FFmpeg runs in a background worker pool | A slow video upload won't freeze the website |
| No caching | Redis caches popular API responses | 1,000 users asking for the same series list = 1 database query, not 1,000 |
| JWT managed in-house | Consider Auth0 or Keycloak | Battle-tested auth with built-in MFA, OAuth2 social login |
| No CDN for video | Cloudflare CDN caches HLS segments | Video loads from the server closest to the user globally |
| Single database | Primary + read replica | Reads (watching) don't compete with writes (uploading) |
| Manual admin only | Automated subscription webhooks | Stripe automatically tells your server when someone pays |

---

## 6. Prioritized Roadmap

### Phase 1 — Make It Launchable (Before Any Real Users)

These must be done before charging a single dollar.

| # | Task | Effort | Why It's Blocking |
|---|------|--------|-------------------|
| 1 | Stripe payment integration | Large | Can't earn money without it |
| 2 | Subscription content enforcement | Medium | Paying users must get exclusive access |
| 3 | Email verification + password reset | Medium | Users will get locked out |
| 4 | Signed video URLs (R2) | Small | Free users could bypass paywall |
| 5 | Rate limiting on auth endpoints | Small | Brute-force password attacks |
| 6 | Upgrade Render to paid plan | Tiny | Free tier sleeps — bad UX |
| 7 | JWT in httpOnly cookie only (remove localStorage) | Small | Security vulnerability |
| 8 | Terms of Service + Privacy Policy pages | Medium | Legal requirement to process payments |
| 9 | GDPR cookie consent banner | Small | Required for EU users |
| 10 | Register consent timestamp in DB | Tiny | Legal audit trail |

### Phase 2 — Make It Good (First 3 Months)

| # | Task | Effort | Impact |
|---|------|--------|--------|
| 11 | Multiple video quality levels (360p/720p/1080p) | Medium | Huge UX improvement, works on slow connections |
| 12 | Series poster image file upload (not URL typing) | Small | Admin UX improvement |
| 13 | Full-text search | Small | Users can find content |
| 14 | Sentry error monitoring | Small | Know about crashes before users report them |
| 15 | Admin user management panel | Medium | Support team needs this |
| 16 | "Continue Watching" on homepage | Small | Core Netflix-like feature |
| 17 | Subtitles/captions upload | Medium | Accessibility compliance |

### Phase 3 — Make It Scale (6+ Months)

| # | Task | Effort | When Needed |
|---|------|--------|-------------|
| 18 | Extract transcoding to background worker + Redis queue | Large | When upload volume grows |
| 19 | PostgreSQL read replica | Medium | When you have 10k+ users |
| 20 | Redis API response caching | Medium | When database load is high |
| 21 | Admin analytics dashboard | Large | Once you have data to show |
| 22 | Social login (Google / Apple sign-in) | Medium | Reduces signup friction |
| 23 | Mobile app (React Native) | Very Large | When web traction is proven |
| 24 | Consider microservices split | Very Large | When team size grows |

---

---

## 7. Implementation Log — 작업 이력

### 2026-03-18 — Phase 1 보안 & 상용화 기반 작업

#### 완료된 작업

| # | 항목 | 파일 | 상태 |
|---|------|------|------|
| 1 | Rate limiting (인증 엔드포인트) | `config/security/RateLimitingFilter.java` | ✅ 완료 |
| 2 | JWT 만료시간 수정 (30분 → 24시간) | `config/security/JwtTokenProvider.java` | ✅ 완료 |
| 3 | JWT에 subscriptionTier 클레임 추가 | `config/security/JwtTokenProvider.java` | ✅ 완료 |
| 4 | 사용자 DB 스키마 확장 (이메일 인증, 비밀번호 재설정, 구독 플랜) | `V8__user_auth_enhancements.sql` | ✅ 완료 |
| 5 | 콘텐츠 접근 등급 컬럼 추가 (FREE/BASIC/PREMIUM) | `V9__content_access_tier.sql` | ✅ 완료 |
| 6 | 이메일 발송 서비스 구현 (Dev: 콘솔 출력, Prod: SMTP) | `application/service/EmailService.java` | ✅ 완료 |
| 7 | 이메일 인증 엔드포인트 (`/auth/verify-email`) | `adapter/in/web/auth/AuthController.java` | ✅ 완료 |
| 8 | 비밀번호 재설정 엔드포인트 (`/auth/forgot-password`, `/auth/reset-password`) | `adapter/in/web/auth/AuthController.java` | ✅ 완료 |
| 9 | httpOnly 쿠키 로그인/로그아웃 (`/auth/login`, `/auth/logout`) | `adapter/in/web/auth/AuthController.java` | ✅ 완료 |
| 10 | 관리자 자격증명 환경변수화 (ADMIN_USERNAME/PASSWORD) | `adapter/in/web/auth/AuthController.java` | ✅ 완료 |
| 11 | 구독 플랜 강제 적용 (재생 시 tier 체크) | `application/service/GetPlaybackService.java` | ✅ 완료 |
| 12 | 재생 엔드포인트 인증 필수화 | `config/security/SecurityConfig.java` | ✅ 완료 |
| 13 | R2 Presigned URL 생성 (영상 URL 보호) | `adapter/out/storage/r2/R2MediaStorageAdapter.java` | ✅ 완료 |
| 14 | S3Presigner 빈 등록 | `config/StorageConfig.java` | ✅ 완료 |
| 15 | ContentAccessPort 구현 (JDBC 어댑터) | `ContentAccessJdbcAdapter.java` | ✅ 완료 |
| 16 | [웹앱] localStorage → httpOnly 쿠키 마이그레이션 | `components/AuthProvider.tsx` | ✅ 완료 |
| 17 | [웹앱] 로그인/회원가입 라우트 쿠키 설정 | `app/api/auth/login|signup|logout/route.ts` | ✅ 완료 |
| 18 | [웹앱] middleware.ts 추가 (보호된 경로 인증 게이트) | `middleware.ts` | ✅ 완료 |
| 19 | [어드민] 하드코딩 URL → 환경변수 전환 | `app/api/auth/admin/login/route.ts` | ✅ 완료 |
| 20 | [어드민] secure 쿠키 조건부 설정 (운영환경만) | `app/api/auth/admin/login/route.ts` | ✅ 완료 |

#### 수정된 파일 목록

**Backend (ai-ott-server):**
- `build.gradle` — spring-boot-starter-mail 의존성 추가
- `src/main/resources/application.yml` — 이메일/웹 기본 URL 설정 추가, JWT secret 환경변수화
- `src/main/resources/db/migration/V8__user_auth_enhancements.sql` — ✨ NEW
- `src/main/resources/db/migration/V9__content_access_tier.sql` — ✨ NEW
- `src/main/java/.../entity/UserJpaEntity.java` — 새 컬럼 필드 추가
- `src/main/java/.../repository/UserJpaRepository.java` — 토큰 조회 메서드 추가
- `src/main/java/.../port/out/UserAuthPort.java` — 인터페이스 확장
- `src/main/java/.../adapter/UserAuthPersistenceAdapter.java` — 새 메서드 구현
- `src/main/java/.../port/out/ContentAccessPort.java` — ✨ NEW
- `src/main/java/.../adapter/ContentAccessJdbcAdapter.java` — ✨ NEW
- `src/main/java/.../service/EmailService.java` — ✨ NEW
- `src/main/java/.../security/RateLimitingFilter.java` — ✨ NEW
- `src/main/java/.../security/JwtTokenProvider.java` — 24h 만료, subscription_tier 클레임
- `src/main/java/.../security/SecurityConfig.java` — Rate limit 필터 등록, 재생 인증 필수
- `src/main/java/.../auth/AuthController.java` — 전면 재작성 (이메일 인증, 비밀번호 재설정, httpOnly 쿠키)
- `src/main/java/.../port/out/MediaStoragePort.java` — getPlaybackUrl() 기본 메서드 추가
- `src/main/java/.../storage/r2/R2MediaStorageAdapter.java` — S3Presigner + getPlaybackUrl() 구현
- `src/main/java/.../config/StorageConfig.java` — S3Presigner 빈 등록
- `src/main/java/.../service/GetPlaybackService.java` — 구독 tier 체크, presigned URL 적용

**Frontend (ai-ott-web):**
- `components/AuthProvider.tsx` — localStorage 제거, 쿠키 기반으로 전환
- `app/api/auth/login/route.ts` — httpOnly 쿠키 설정
- `app/api/auth/signup/route.ts` — httpOnly 쿠키 설정
- `app/api/auth/logout/route.ts` — 쿠키 제거
- `middleware.ts` — ✨ NEW (보호된 경로 인증 게이트)

**Frontend (ai-ott-admin):**
- `src/app/api/auth/admin/login/route.ts` — 환경변수 기반 URL, 조건부 secure 쿠키

#### Phase 1 완료율

| 항목 | 완료 여부 |
|------|----------|
| Stripe 결제 통합 | ⬜ 미완료 (다음 우선순위) |
| 구독 콘텐츠 접근 제한 (DB + 서비스) | ✅ 완료 |
| 이메일 인증 인프라 | ✅ 완료 |
| 비밀번호 재설정 | ✅ 완료 |
| Signed video URLs (R2 presigned) | ✅ 완료 |
| Rate limiting (auth 엔드포인트) | ✅ 완료 |
| JWT httpOnly 쿠키 전환 | ✅ 완료 |
| JWT 만료시간 수정 (24h) | ✅ 완료 |
| 관리자 자격증명 환경변수화 | ✅ 완료 |

---

### 2026-03-18 — Phase 1 완료 및 Phase 2 시작

#### 완료된 작업 (이어서)

| # | 항목 | 파일 | 상태 |
|---|------|------|------|
| 21 | Stripe Java SDK 의존성 추가 | `build.gradle` | ✅ 완료 |
| 22 | Stripe 설정 Properties 클래스 | `config/StripeProperties.java` | ✅ 완료 |
| 23 | Stripe DB 스키마 (stripe_customer_id 등) | `V10__stripe_integration.sql` | ✅ 완료 |
| 24 | UserSubscriptionPort + JDBC 어댑터 | `UserSubscriptionJdbcAdapter.java` | ✅ 완료 |
| 25 | PaymentService (Checkout, Portal, Webhook) | `application/service/PaymentService.java` | ✅ 완료 |
| 26 | PaymentController (`/api/app/payments/*`) | `adapter/in/web/app/PaymentController.java` | ✅ 완료 |
| 27 | StripeWebhookController (`/api/webhooks/stripe`) | `adapter/in/web/app/StripeWebhookController.java` | ✅ 완료 |
| 28 | SecurityConfig — webhook chain (인증 불필요) | `config/security/SecurityConfig.java` | ✅ 완료 |
| 29 | [웹앱] 가격 페이지 (`/pricing`) | `app/pricing/page.tsx` | ✅ 완료 |
| 30 | [웹앱] 결제 성공 페이지 (`/payment/success`) | `app/payment/success/page.tsx` | ✅ 완료 |
| 31 | [웹앱] 결제 취소 페이지 (`/payment/cancel`) | `app/payment/cancel/page.tsx` | ✅ 완료 |
| 32 | [웹앱] 결제 프록시 라우트 | `app/api/payments/checkout/route.ts` | ✅ 완료 |
| 33 | [웹앱] 이용약관 페이지 (`/legal/terms`) | `app/legal/terms/page.tsx` | ✅ 완료 |
| 34 | [웹앱] 개인정보처리방침 페이지 (`/legal/privacy`) | `app/legal/privacy/page.tsx` | ✅ 완료 |
| 35 | [웹앱] GDPR 쿠키 동의 배너 | `components/CookieConsent.tsx` | ✅ 완료 |
| 36 | [웹앱] 쿠키 배너 레이아웃 통합 | `app/layout.tsx` | ✅ 완료 |
| 37 | [웹앱] 회원가입 이용약관 동의 체크박스 | `components/LoginModal.tsx` | ✅ 완료 |

#### Phase 2 작업 (이 세션)

| # | 항목 | 파일 | 상태 |
|---|------|------|------|
| 38 | 어드민 유저 관리 API (목록/등급변경/삭제) | `AdminUserController.java`, `AdminUserService.java`, `AdminUserJdbcAdapter.java` | ✅ 완료 |
| 39 | 어드민 유저 관련 포트/DTO | `AdminUserSummary.java`, `AdminUserQueryPort.java`, `AdminUserCommandPort.java` | ✅ 완료 |
| 40 | Watch Progress DB 스키마 | `V11__user_watch_progress.sql` | ✅ 완료 |
| 41 | MeController (`/api/app/me/*`) — 이어보기, 재생 위치 | `MeController.java` | ✅ 완료 |
| 42 | UserWatchProgressService + 포트/어댑터 | `UserWatchProgressService.java`, `UserWatchProgressJdbcAdapter.java` | ✅ 완료 |
| 43 | SecurityConfig — `/api/app/me/**` 인증 필수 추가 | `SecurityConfig.java` | ✅ 완료 |
| 44 | [웹앱] 홈페이지 "이어보기" 섹션 | `app/page.tsx` | ✅ 완료 |

#### Phase 1 완료율 (갱신)

| 항목 | 완료 여부 |
|------|----------|
| Stripe 결제 통합 | ✅ 완료 |
| 구독 콘텐츠 접근 제한 | ✅ 완료 |
| 이메일 인증 인프라 | ✅ 완료 |
| 비밀번호 재설정 | ✅ 완료 |
| Signed video URLs (R2 presigned) | ✅ 완료 |
| Rate limiting (auth 엔드포인트) | ✅ 완료 |
| JWT httpOnly 쿠키 전환 | ✅ 완료 |
| ToS + Privacy Policy 페이지 | ✅ 완료 |
| GDPR 쿠키 동의 배너 | ✅ 완료 |
| 회원가입 이용약관 동의 | ✅ 완료 |
| Render 유료 플랜 전환 | ⬜ 수동 작업 필요 (Render 대시보드) |

#### 남은 Phase 2 항목

1. **[어드민] 유저 관리 페이지** — 백엔드 API는 완료, 어드민 UI 페이지 미구현
2. **Sentry 에러 모니터링** — 프론트엔드 + 백엔드 통합
3. **다중 화질 HLS 인코딩** — FFmpeg 멀티 비트레이트 (360p/720p/1080p)
4. **자막/캡션 기능** — SRT/VTT 업로드 + 플레이어 선택 UI

---

## Summary: The Honest Status

> Think of building a house. The foundation is poured, the walls are up, the rooms are laid out, and the furniture is inside. But the house has no front door lock, no running water (payments), and no address on file with the post office (email). You wouldn't rent it out yet — but you're much closer than most people ever get.

**What you have**: A functionally complete streaming platform that works end-to-end — admins can upload video, it transcodes to HLS, users can browse and watch. The core is real.

**What's missing**: The commerce layer (payments, paywalls, emails) and the security hardening needed before real money changes hands.

**Realistic path to launch**: Phases 1 items above, completed in order. The payment integration is the biggest single task (2-3 weeks for a solid Stripe integration). Everything else in Phase 1 can realistically be done in parallel by a small team in 4-6 weeks.
