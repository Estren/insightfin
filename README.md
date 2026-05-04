# 🌅 Orizon

[![CI](https://github.com/Estren/orizon/actions/workflows/ci.yml/badge.svg)](https://github.com/Estren/orizon/actions/workflows/ci.yml)

Personal financial management platform — simple, visual, and powered by AI.

Orizon helps users track expenses, set financial goals, and receive personalized insights to improve their financial health.

---

## 📦 Monorepo Structure

| Directory | Description | Stack |
|---|---|---|
| `core-api/` | 🔧 Main REST API — manages users, transactions, categories, goals, budgets, and AI feedback | Java 17, Quarkus 3.17, PostgreSQL |
| `frontend/web/` | 💻 Web client (admin dashboard) | Angular 21, Tailwind CSS 4, RxJS, ApexCharts |
| `frontend/mobile/` | 📱 Mobile client — reserved for future development | TBD |
| `ai/` | 🧠 AI reasoning service — financial feedback and insights | Python, FastAPI, Azure OpenAI, aiokafka, APScheduler |

## 🚀 Getting Started

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & Docker Compose
- [Node.js](https://nodejs.org/) 20+ and npm (for web frontend development)
- [Make](https://www.gnu.org/software/make/) (optional, for convenience commands)
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (optional, for Kubernetes commands)

### Quick Start

```bash
# Start all backend services (dev mode with hot reload)
make up

# Install and run the web frontend locally
make frontend-install
make frontend-run
```

### Available Commands

| Command | Description |
|---|---|
| `make up` | 🟢 Start all services (dev mode with hot reload) |
| `make down` | 🔴 Stop all services |
| `make up-db` | 🗄️ Start only PostgreSQL |
| `make up-api` | 🔧 Start core-api + PostgreSQL |
| `make up-ai` | 🧠 Start ai + core-api + PostgreSQL |
| `make build` | 🏗️ Rebuild all Docker images |
| `make logs` | 📋 Show logs from all services |
| `make frontend-install` | 📥 Install Angular frontend dependencies |
| `make frontend-run` | 💻 Run Angular frontend locally (`ng serve`) |
| `make frontend-build` | 📦 Build Angular frontend for production |
| `make test-api` | 🧪 Run core-api tests (Maven) |
| `make clean` | 🧹 Stop services and remove volumes |
| `make help` | ❓ Show all available commands |
| `make k8s-deploy-api` | ☸️ Rebuild core-api image and redeploy to local cluster |
| `make k8s-deploy-ai` | ☸️ Rebuild ai-service image and redeploy to local cluster |
| `make k8s-status` | ☸️ Show pod status in orizon namespace |
| `make k8s-logs` | ☸️ Stream logs from core-api pods |

## 🔗 Service URLs

| Service | URL |
|---|---|
| Core API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Web Frontend | `http://localhost:4200` |
| AI Service | `http://localhost:8081` |
| PostgreSQL | `localhost:5432` |

## ☸️ Kubernetes (Local Cluster)

The k8s manifests in `k8s/` describe the full production-like setup (core-api, ai-service, PostgreSQL, Kafka, Ingress). Use this to validate behaviour that Docker Compose dev mode cannot replicate: rolling deploys, health probes, HPA, and Ingress routing.

**Prerequisites:** Docker Desktop with Kubernetes enabled (or kind/kubeadm).

### First-time setup

```bash
# Apply all manifests once
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/core-api/
kubectl apply -f k8s/ai-service/
kubectl apply -f k8s/ingress.yaml
```

### Deploy workflow (after code changes)

```bash
# 1. Rebuild image and redeploy
make k8s-deploy-api     # core-api
make k8s-deploy-ai      # ai-service

# 2. Watch pods come up
make k8s-status

# 3. Validate
curl http://localhost/q/health/live   # → {"status":"UP"}
curl -X POST http://localhost/api/auth/register ...
```

### Service URLs (via Ingress)

| Path | Service |
|---|---|
| `http://localhost/api/...` | core-api REST endpoints |
| `http://localhost/q/health/live` | core-api liveness probe |
| `http://localhost/q/health/ready` | core-api readiness probe |

---

## 🏛️ Architecture

The `core-api` follows **Hexagonal Architecture (Ports & Adapters)**:

```
Request → Adapter (in/web) → UseCase (application) → Domain → Port (out) → Adapter (out/persistence) → Database
```

- **Domain layer** — Pure business logic, no framework dependencies
- **Application layer** — Use cases and ports (input/output contracts)
- **Adapter layer** — Web controllers, DTOs, JPA entities, persistence mappers
- **Config layer** — Framework configuration, security, beans

## 📊 Database Entities

```
User ──┬── Transaction ── Category
       ├── Goal ── GoalContribution
       ├── Budget ── Category
       ├── AiFeedback
       └── RefreshToken
```

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Quarkus 3.17, Hibernate ORM with Panache, RESTEasy Reactive, JWT (JJWT) |
| Web Frontend | Angular 21, Tailwind CSS 4, RxJS, ApexCharts, Playwright (e2e) |
| Mobile Frontend | Reserved for future development |
| AI | Python 3.12, FastAPI 0.115, Azure OpenAI (GPT-4o-mini), aiokafka, APScheduler, Prometheus |
| Database | PostgreSQL 16, Flyway migrations |
| Build | Maven (core-api), npm / Angular CLI (frontend) |
| Infra | Docker, Docker Compose, Make |
| Docs | Swagger / OpenAPI 3 |

## 🧪 Testing

### core-api

The `core-api` has a suite of unit and integration tests covering business logic and HTTP contracts.

**Unit tests (48)** — pure JUnit 5 + Mockito + AssertJ, no database, no Kafka, no Quarkus context:

| Class | Tests | Coverage |
|---|---|---|
| `TransactionServiceTest` | 8 | create, list, update, delete — ownership checks + event publishing |
| `BudgetServiceTest` | 8 | create, list, status (% calculation + division-by-zero guard), update, delete |
| `GoalServiceTest` | 8 | create, contribute (target completion + event), update, delete |
| `DashboardServiceTest` | 4 | empty month, totals calculation, recent-5 limit, completed goals excluded |
| `CoreApiApplicationTests` | 1 | Application context smoke test |

**Integration tests (28)** — `@QuarkusTest` + REST-Assured + H2 in-memory database:

| Class | Tests | Coverage |
|---|---|---|
| `AuthControllerIT` | 11 | register, login, refresh, logout — happy path + 400/401/409 |
| `TransactionControllerIT` | 9 | CRUD + 401 without token + cross-tenant 404 |
| `CategoryControllerIT` | 8 | CRUD + 401 without token + cross-tenant 404 |

### CI (GitHub Actions)

Every push and PR to `main` triggers two parallel jobs:

| Job | What it does |
|---|---|
| `Core API — Unit Tests` | Runs all 76 tests (unit + integration) via Maven |
| `Frontend — Lint & Build` | `npm ci` → `ng lint` → `ng build` (production) |

**Run via Make (from monorepo root):**

```bash
make test-api
```

**Run directly (from `core-api/`):**

```bash
# Windows
mvnw.cmd test
mvnw.cmd test -Dtest=TransactionServiceTest

# Linux / macOS
./mvnw test
./mvnw test -Dtest=TransactionServiceTest
```

---

## 📄 License

This project is private and not licensed for public use.
