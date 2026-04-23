# 🌅 Orizon

Personal financial management platform — simple, visual, and powered by AI.

Orizon helps users track expenses, set financial goals, and receive personalized insights to improve their financial health.

---

## 📦 Monorepo Structure

| Directory | Description | Stack |
|---|---|---|
| `core-api/` | 🔧 Main REST API — manages users, transactions, categories, goals, budgets, and AI feedback | Java 17, Quarkus 3.17, PostgreSQL |
| `frontend/web/` | 💻 Web client (admin dashboard) | Angular 21, Tailwind CSS 4, RxJS, ApexCharts |
| `frontend/mobile/` | 📱 Mobile client — reserved for future development | TBD |
| `ai/` | 🧠 AI reasoning service — financial feedback and insights | Python (framework TBD) |

## 🚀 Getting Started

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & Docker Compose
- [Node.js](https://nodejs.org/) 20+ and npm (for web frontend development)
- [Make](https://www.gnu.org/software/make/) (optional, for convenience commands)

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

## 🔗 Service URLs

| Service | URL |
|---|---|
| Core API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Web Frontend | `http://localhost:4200` |
| AI Service | `http://localhost:8081` |
| PostgreSQL | `localhost:5432` |

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
| AI | Python (framework TBD) |
| Database | PostgreSQL 16, Flyway migrations |
| Build | Maven (core-api), npm / Angular CLI (frontend) |
| Infra | Docker, Docker Compose, Make |
| Docs | Swagger / OpenAPI 3 |

## 📄 License

This project is private and not licensed for public use.
