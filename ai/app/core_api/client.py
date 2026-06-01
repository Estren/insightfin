from __future__ import annotations

import asyncio
from typing import Optional
from uuid import UUID

import httpx
import structlog

from app.config import settings
from app.core_api.models import (
    BudgetStatusResponse,
    CategoryResponse,
    GoalResponse,
    TransactionResponse,
    UserSummary,
)

log = structlog.get_logger(__name__)


class CoreApiClient:
    def __init__(self, client: httpx.AsyncClient) -> None:
        self._client = client

    async def _get(self, path: str, params: Optional[dict] = None) -> dict | list:
        url = f"{settings.core_api_url}{path}"
        response = await self._client.get(url, params=params)
        response.raise_for_status()
        return response.json()

    async def list_users(self) -> list[UserSummary]:
        data = await self._get("/internal/users")
        return [UserSummary(id=uid) for uid in data]

    async def get_transactions(self, user_id: UUID, month: str) -> list[TransactionResponse]:
        data = await self._get(f"/internal/users/{user_id}/transactions", {"month": month})
        return [TransactionResponse(**item) for item in data]

    async def get_budgets(self, user_id: UUID, month: str) -> list[BudgetStatusResponse]:
        data = await self._get(f"/internal/users/{user_id}/budgets", {"month": month})
        return [BudgetStatusResponse(**item) for item in data]

    async def get_goals(self, user_id: UUID) -> list[GoalResponse]:
        data = await self._get(f"/internal/users/{user_id}/goals")
        return [GoalResponse(**item) for item in data]

    async def get_categories(self, user_id: UUID) -> list[CategoryResponse]:
        data = await self._get(f"/internal/users/{user_id}/categories")
        return [CategoryResponse(**item) for item in data]

    async def get_user_context(self, user_id: UUID, month: str) -> dict:
        """Fetch all financial data for a user in parallel."""
        transactions, budgets, goals, categories = await asyncio.gather(
            self.get_transactions(user_id, month),
            self.get_budgets(user_id, month),
            self.get_goals(user_id),
            self.get_categories(user_id),
            return_exceptions=True,
        )

        def unwrap(result, label: str):
            if isinstance(result, Exception):
                log.warning("core_api_fetch_failed", resource=label, error=str(result))
                return []
            return result

        return {
            "transactions": unwrap(transactions, "transactions"),
            "budgets": unwrap(budgets, "budgets"),
            "goals": unwrap(goals, "goals"),
            "categories": unwrap(categories, "categories"),
        }

    async def post_feedback(self, payload: dict) -> None:
        url = f"{settings.core_api_url}/internal/feedbacks"
        response = await self._client.post(url, json=payload)
        response.raise_for_status()


def make_http_client() -> httpx.AsyncClient:
    # /internal/* endpoints on core-api now require the shared secret; without
    # it every call returns 401. The header is sent on every request so the
    # Coach Agent's tool calls and the batch orchestrator both authenticate
    # without any per-call boilerplate.
    headers = {"X-Internal-Auth": settings.internal_shared_secret} if settings.internal_shared_secret else {}
    return httpx.AsyncClient(timeout=httpx.Timeout(30.0), headers=headers)
