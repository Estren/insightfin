# 🌅 Orizon

Personal financial management platform — simple, visual, and powered by AI.

Orizon helps users track expenses, set financial goals, and receive personalized insights to improve their financial health.

---

## 📦 Monorepo Structure

| Directory | Description | Stack |
|---|---|---|
| `core-api/` | 🔧 Main REST API — manages users, transactions, categories, goals, and budgets | Java 21, Spring Boot, PostgreSQL |
| `frontend/` | 📱 Mobile & web client | Flutter, BLoC, Dio |
| `ai/` | 🧠 AI reasoning service — financial feedback and insights | Python (TBD) |

## 🚀 Getting Started

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & Docker Compose
- [Flutter SDK](https://docs.flutter.dev/get-started/install) (for frontend development)
- [Make](https://www.gnu.org/software/make/) (optional, for convenience commands)

### Quick Start

```bash
# Start all backend services (dev mode with hot reload)
make up

# Run the frontend locally
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
| `make frontend-run` | 📱 Run Flutter frontend locally |
| `make frontend-build` | 📦 Build Flutter frontend for web |
| `make test-api` | 🧪 Run core-api tests |
| `make clean` | 🧹 Stop services and remove volumes |
| `make help` | ❓ Show all available commands |

## 🔗 Service URLs

| Service | URL |
|---|---|
| Core API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| AI Service | `http://localhost:8081` |
| PostgreSQL | `localhost:5432` |

## 🏛️ Architecture

The project follows **Clean Architecture** principles across all services:

```
Request → Controller → UseCase → Service → Repository → Database
```

- **Domain layer** — Pure business logic, no framework dependencies
- **Application layer** — Use case implementations
- **Adapter layer** — Controllers, DTOs, JPA entities, external integrations
- **Config layer** — Framework configuration, security, beans

## 📊 Database Entities

```
User ──┬── Transaction ── Category
       ├── Goal ── GoalContribution
       └── Budget ── Category
```

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.4, Spring Security, JWT |
| Frontend | Flutter, BLoC, Dio, GoRouter, GetIt |
| AI | Python (framework TBD) |
| Database | PostgreSQL 16, Flyway migrations |
| Infra | Docker, Docker Compose, Make |
| Docs | Swagger / OpenAPI 3 |

## 📄 License

This project is private and not licensed for public use.
