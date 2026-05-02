from __future__ import annotations

import asyncio

import structlog
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger

from app.config import settings

log = structlog.get_logger(__name__)


def _parse_cron(expr: str) -> CronTrigger:
    minute, hour, day, month, day_of_week = expr.split()
    return CronTrigger(
        minute=minute,
        hour=hour,
        day=day,
        month=month,
        day_of_week=day_of_week,
    )


class Scheduler:
    def __init__(self, orchestrator) -> None:
        self._orchestrator = orchestrator
        self._scheduler = AsyncIOScheduler()

    def start(self) -> None:
        trigger = _parse_cron(settings.monthly_cron)
        self._scheduler.add_job(
            self._run_monthly,
            trigger=trigger,
            id="monthly_batch",
            replace_existing=True,
        )
        self._scheduler.start()
        log.info("scheduler_started", cron=settings.monthly_cron)

    def stop(self) -> None:
        self._scheduler.shutdown(wait=False)
        log.info("scheduler_stopped")

    async def _run_monthly(self) -> None:
        try:
            await self._orchestrator.run_monthly_batch()
        except Exception as exc:
            log.error("monthly_batch_error", error=str(exc))
