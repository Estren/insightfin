.PHONY: up down up-db up-api up-ai build logs clean frontend-run frontend-build test-api help

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

# === Frontend ===

frontend-run: ## Run Flutter frontend locally
	cd frontend && flutter run

frontend-build: ## Build Flutter frontend
	cd frontend && flutter build web

# === Testing ===

test-api: ## Run core-api tests
	cd core-api && ./gradlew test

# === Cleanup ===

clean: ## Stop services and remove volumes
	docker compose down -v

# === Help ===

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
