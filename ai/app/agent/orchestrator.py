from __future__ import annotations

from datetime import date
from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)

ANALYSIS_TYPES = frozenset({"MONTHLY_REPORT", "HEALTH_SCORE", "ALERT", "GOAL_PROJECTION"})


class Orchestrator:
    def __init__(self, core_api: CoreApiClient) -> None:
        self._core_api = core_api

    async def analyze(
        self,
        user_id: UUID,
        analysis_type: str,
        month: str | None = None,
        force: bool = False,
    ) -> dict:
        if analysis_type not in ANALYSIS_TYPES:
            raise ValueError(f"Unknown analysis type: {analysis_type}")

        ref_month = month or date.today().strftime("%Y-%m")

        log.info(
            "analysis_started",
            user_id=str(user_id),
            type=analysis_type,
            month=ref_month,
            force=force,
        )

        context = await self._core_api.get_user_context(user_id, ref_month)

        has_data = any(
            len(v) > 0 for v in context.values() if isinstance(v, list)
        )

        if not has_data:
            log.info("analysis_skipped_no_data", user_id=str(user_id), type=analysis_type)
            return {"status": "skipped", "reason": "no_data"}

        log.info(
            "analysis_context_ready",
            user_id=str(user_id),
            type=analysis_type,
            transactions=len(context["transactions"]),
            budgets=len(context["budgets"]),
            goals=len(context["goals"]),
            categories=len(context["categories"]),
        )

        # Phase 2 mock — LLM call will replace this in Phase 3
        log.info(
            "analysis_mock_llm",
            user_id=str(user_id),
            type=analysis_type,
            message="[MOCK] Would call LLM here — Phase 3 will implement the real call",
        )

        return {
            "status": "mock",
            "user_id": str(user_id),
            "type": analysis_type,
            "month": ref_month,
            "context_summary": {
                "transactions": len(context["transactions"]),
                "budgets": len(context["budgets"]),
                "goals": len(context["goals"]),
                "categories": len(context["categories"]),
            },
        }

    async def run_monthly_batch(self) -> None:
        month = date.today().strftime("%Y-%m")
        log.info("monthly_batch_started", month=month)

        users = await self._core_api.list_users()
        log.info("monthly_batch_users", count=len(users))

        for user in users:
            for analysis_type in ("MONTHLY_REPORT", "HEALTH_SCORE"):
                try:
                    await self.analyze(user.id, analysis_type, month=month)
                except Exception as exc:
                    log.error(
                        "monthly_batch_user_failed",
                        user_id=str(user.id),
                        type=analysis_type,
                        error=str(exc),
                    )

        log.info("monthly_batch_finished", month=month)
