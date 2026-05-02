from __future__ import annotations

from datetime import date
from uuid import UUID

import structlog
from prometheus_client import Counter

from app.agent import context_builder
from app.agent.llm_client import LLMClient, LLMDailyLimitError
from app.core_api.client import CoreApiClient
from app.prompts import loader as prompt_loader

log = structlog.get_logger(__name__)

ANALYSIS_TYPES = frozenset({"MONTHLY_REPORT", "HEALTH_SCORE", "ALERT", "GOAL_PROJECTION"})

_analysis_counter = Counter(
    "ai_analysis_total",
    "Total AI analyses by type and outcome",
    ["type", "status"],
)


class Orchestrator:
    def __init__(self, core_api: CoreApiClient, llm: LLMClient) -> None:
        self._core_api = core_api
        self._llm = llm

    async def analyze(
        self,
        user_id: UUID,
        analysis_type: str,
        month: str | None = None,
        force: bool = False,
    ) -> None:
        if analysis_type not in ANALYSIS_TYPES:
            raise ValueError(f"Unknown analysis type: {analysis_type}")

        ref_month = month or date.today().strftime("%Y-%m")

        log.info(
            "analysis_started",
            user_id=str(user_id),
            type=analysis_type,
            month=ref_month,
        )

        context = await self._core_api.get_user_context(user_id, ref_month)

        has_data = any(len(v) > 0 for v in context.values() if isinstance(v, list))
        if not has_data:
            log.info("analysis_skipped", reason="no_data", user_id=str(user_id), type=analysis_type)
            _analysis_counter.labels(type=analysis_type, status="skipped").inc()
            return

        built = context_builder.build(analysis_type, context, ref_month)
        if built is None:
            log.info("analysis_skipped", reason="no_trigger", user_id=str(user_id), type=analysis_type)
            _analysis_counter.labels(type=analysis_type, status="skipped").inc()
            return

        system_prompt = prompt_loader.load(analysis_type)

        try:
            result = await self._llm.generate(system_prompt, built["prompt_context"])
        except LLMDailyLimitError:
            log.warning("analysis_skipped", reason="daily_limit", user_id=str(user_id), type=analysis_type)
            _analysis_counter.labels(type=analysis_type, status="skipped").inc()
            return
        except Exception as exc:
            log.error("analysis_llm_failed", user_id=str(user_id), type=analysis_type, error=str(exc))
            _analysis_counter.labels(type=analysis_type, status="failed").inc()
            raise

        metadata = built["computed_metadata"]
        if analysis_type == "MONTHLY_REPORT" and "highlights" in result:
            metadata["highlights"] = result.get("highlights", [])

        feedback_payload = {
            "userId": str(user_id),
            "type": analysis_type,
            "title": result.get("title", analysis_type),
            "content": result.get("content", ""),
            "metadata": metadata,
            "referenceMonth": ref_month,
        }

        try:
            await self._core_api.post_feedback(feedback_payload)
        except Exception as exc:
            if hasattr(exc, "response") and exc.response.status_code == 409:
                log.info("analysis_skipped", reason="idempotent", user_id=str(user_id), type=analysis_type)
                _analysis_counter.labels(type=analysis_type, status="skipped").inc()
                return
            log.error("analysis_post_failed", user_id=str(user_id), type=analysis_type, error=str(exc))
            _analysis_counter.labels(type=analysis_type, status="failed").inc()
            raise

        _analysis_counter.labels(type=analysis_type, status="completed").inc()
        log.info("analysis_completed", user_id=str(user_id), type=analysis_type, month=ref_month)

    async def run_monthly_batch(self) -> None:
        import asyncio
        import random

        month = date.today().strftime("%Y-%m")
        log.info("monthly_batch_started", month=month)

        users = await self._core_api.list_users()
        log.info("monthly_batch_users", count=len(users))

        batch_size = 10
        batches = [users[i : i + batch_size] for i in range(0, len(users), batch_size)]

        for batch in batches:
            await asyncio.gather(
                *[self.analyze(u.id, "MONTHLY_REPORT", month=month) for u in batch],
                return_exceptions=True,
            )
            await asyncio.sleep(random.uniform(1.0, 3.0))

            await asyncio.gather(
                *[self.analyze(u.id, "HEALTH_SCORE", month=month) for u in batch],
                return_exceptions=True,
            )
            await asyncio.sleep(random.uniform(1.0, 3.0))

        log.info("monthly_batch_finished", month=month)
