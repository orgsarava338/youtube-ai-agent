# Project Status Record

Date: 2026-10-17
Branch: `main` (clean working tree, up to date with `origin/main`)
Commit: d7dd19b "road map"

## Roadmap Progress

| Milestone | Status | Summary |
|-----------|--------|---------|
| M0 — Baseline verification | ✅ Complete | Code compiles, all tests pass, frontend builds, app boots, DB + Flyway + health + security verified |
| M1 — Connect and identify YouTube channel | ✅ Complete | OAuth start/callback, token exchange/refresh, encryption, channel fetch — all verified |
| M2 — Build the 30-day analytics service | ✅ Complete | Analytics service, report DTO, REST controller, frontend API + UI — all verified |

## Toolchain
- Java: openjdk 25.0.2 LTS — matches `pom.xml` `<java.version>25</java.version>`
- Maven: Apache Maven 3.8.7 (system `mvn`; `./mvnw` wrapper is broken)
- Node: v24.21.0, Bun: 1.3.14

## Backend (89 tests, 0 failures, 0 errors)
- Compilation: clean (Java 25, Lombok annotation processing)
- `mvn test`: all test classes pass
- App starts on `local` profile in ~8–9s; health endpoint `{"status":"UP"}`
- Flyway: 6 migrations, schema v6, up to date
- PostgreSQL: `assistube-postgres` container on localhost:5432

## M2 — 30-day analytics service details

**Backend:**
- `YouTubeAnalyticsService.getLast30DaysReport(userId)` — computes last 30 completed days (end=yesterday, start=29 days prior), fetches channel analytics + top 10 videos
- `YouTubeAnalyticsService.getMyChannelAnalytics()` / `getMyVideoAnalytics()` / `getMyTopVideoAnalytics()` — existing, with pagination support for top videos (follows `nextPageToken`)
- Error handling: 401/403 → "reconnect your YouTube account"; 403 quota → API error message; 5xx → server error message
- `YouTubeAnalyticsReport` record — combines channel analytics + top videos, with `hasData()` helper
- `YouTubeAnalyticsController` — `GET /api/v1/youtube/analytics/last-30-days` (protected, requires auth)

**Frontend:**
- `web/src/features/analytics/types/analytics.d.ts` — TypeScript interfaces
- `web/src/features/analytics/api/analyticsApi.ts` — `getLast30DaysReport()` API call
- `web/src/features/analytics/hooks/useAnalytics.ts` — TanStack Query hook
- `web/src/features/analytics/pages/AnalyticsPage.tsx` + `.scss` — analytics dashboard with channel metrics + top videos table
- `web/src/features/chat/components/ChatSidebar.tsx` — Analytics nav item enabled (no longer "Coming soon"), `onViewAnalytics` callback
- `web/src/features/chat/pages/ChatPage.tsx` — navigates to `/workspace/:customUrl/analytics` on analytics click
- `web/src/App.tsx` — added `/workspace/:customUrl/analytics` route

**Build:** `bun run build` ✅ (TypeScript + Vite), `bun run lint` ✅ (only pre-existing warnings)

## Known issues / limitations
| # | Issue | Impact |
|---|-------|--------|
| 1 | `./mvnw` wrapper missing `.mvn/wrapper/maven-wrapper.properties` | Use system `mvn` |
| 2 | Authenticated flows need a real Google OAuth access token (no dev bypass) | Requires user credentials for end-to-end testing |
| 3 | YouTube Analytics data requires a real authorized channel + API quota | Deferred to integration testing (M9) |

## Next: Milestone 3 — Implement the recommendation engine
Pass analytics + top-video info to the AI, generate 5 distinct video ideas, score each, explain reasoning.
