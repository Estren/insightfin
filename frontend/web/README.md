# InsightFin Web

Angular 21 + Tailwind CSS 4 frontend for **InsightFin**. Consumes the `core-api` over REST. See the top-level [README](../../README.md) for the broader project context.

## Quick start

```bash
npm install
npm start              # ng serve on http://localhost:4200
npm run build          # production build → dist/insightfin/browser/
npm test               # unit tests (Karma + Jasmine)
npm run test:e2e       # e2e (Playwright, mocked API)
npm run lint           # ESLint
npm run prettier       # format src/
```

## Stack

| | |
|---|---|
| **Framework** | Angular 21 — standalone components, signals, OnPush |
| **Styling** | Tailwind CSS 4 — theme tokens (`--primary`, `--background`, `--shimmer-base`, …) defined in `src/styles.css` |
| **Charts** | ApexCharts via `ng-apexcharts` |
| **i18n** | `@ngx-translate/core` — `pt-BR` and `en-US` |
| **Error monitoring** | Sentry (`@sentry/angular`) — no-op when `SENTRY_DSN` is unset |
| **Analytics** | PostHog (cookieless) via `AnalyticsService` |
| **Quality gates** | ESLint (`angular-eslint`) + Prettier + Husky pre-commit |

## Layout

```
src/app/
├── core/        # services, interceptors, guards, models, stores
├── modules/     # feature modules (lazy-loaded)
└── shared/      # reusable UI components
```

Project-specific conventions (theme palette, loading skeletons, write actions, etc.) live in `CLAUDE.md` next to this file.
