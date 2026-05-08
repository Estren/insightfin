.PHONY: up down up-db up-api up-ai build logs clean frontend-install frontend-run frontend-build test-api test-ai setup-ai docs prometheus grafana k8s-deploy-api k8s-deploy-ai k8s-status k8s-logs help

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

# === Docs / Monitoring ===

docs: ## Open Swagger UI (core-api)
	cmd /c start http://localhost:8080/swagger-ui.html/

prometheus: ## Open Prometheus UI
	cmd /c start http://localhost:9090

grafana: ## Open Grafana (admin / insightfin)
	cmd /c start http://localhost:3000

# === Testing ===

test-api: ## Run core-api tests (Maven)
	cd core-api && mvnw.cmd test

setup-ai: ## Install AI service dependencies via pip (run once)
	pip install -r ai/requirements.txt -r ai/requirements-dev.txt -q

test-ai: ## Run AI service tests (requires: make setup-ai)
	cd ai && pytest -v

# === Kubernetes ===

k8s-deploy-api: ## [k8s] Rebuild core-api image and redeploy to local cluster
	docker build -t insightfin-core-api:latest ./core-api
	kubectl rollout restart deployment/core-api -n insightfin

k8s-deploy-ai: ## [k8s] Rebuild ai-service image and redeploy to local cluster
	docker build -t insightfin-ai:latest ./ai
	kubectl rollout restart deployment/ai-service -n insightfin

k8s-status: ## [k8s] Show pod status in insightfin namespace
	kubectl get pods -n insightfin

k8s-logs: ## [k8s] Stream logs from core-api pods
	kubectl logs -f -l app.kubernetes.io/name=core-api -n insightfin --max-log-requests=5

# === Cleanup ===

clean: ## Stop services and remove volumes
	docker compose down -v

# === Help ===

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
