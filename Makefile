.PHONY: up down up-db up-api up-ai build logs clean frontend-install frontend-run frontend-build test-api help

# === Full Stack ===

up: ## Start all services (dev mode with hot reload)
	docker compose up -d

down: ## Stop all services
	docker compose down

build: ## Rebuild all images
	docker compose build

logs: ## Show logs from all services
	docker compose logs -f

# === Individual Services ===

up-db: ## Start only PostgreSQL
	docker compose up -d postgres

up-api: ## Start core-api + PostgreSQL (dev mode)
	docker compose up -d postgres core-api

up-ai: ## Start ai + core-api + PostgreSQL
	docker compose up -d postgres core-api ai

# === Frontend (Angular) ===

frontend-install: ## Install frontend dependencies
	cd frontend/web && npm install

frontend-run: ## Run Angular frontend locally (ng serve)
	cd frontend/web && npm start

frontend-build: ## Build Angular frontend for production
	cd frontend/web && npm run build

# === Docs ===

docs: ## Open Swagger UI in the browser, for Linux/Mac use xdg-open, for Windows use start
	cmd /c start http://localhost:8080/swagger-ui.html/

# === Testing ===

test-api: ## Run core-api tests (Maven)
	cd core-api && ./mvnw test

# === Cleanup ===

clean: ## Stop services and remove volumes
	docker compose down -v

# === Help ===

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
