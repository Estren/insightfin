from __future__ import annotations

from datetime import date
from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def get_goals(core_api: CoreApiClient, user_id: UUID) -> dict:
    """List the user's financial goals with progress and pacing fields.

    For each goal returns: progressPercentage, remainingAmount,
    daysUntilDeadline, monthlyContributionNeeded (to hit target by deadline),
    and a status flag — 'completed' / 'deadline_passed' / 'active' / 'no_deadline'.
    The agent uses these to recommend priorities ("which goal should I focus on?")
    without having to compute pacing math itself.
    """
    goals = await core_api.get_goals(user_id)
    today = date.today()

    items: list[dict] = []
    for g in goals:
        target = float(g.targetAmount)
        current = float(g.currentAmount)
        remaining = max(0.0, target - current)
        progress_pct = (current / target * 100) if target > 0 else 0.0

        if g.deadline is None:
            days_until = None
            monthly_needed = None
            status = "completed" if current >= target else "no_deadline"
        elif current >= target:
            days_until = (g.deadline - today).days
            monthly_needed = 0.0
            status = "completed"
        else:
            days_until = (g.deadline - today).days
            if days_until <= 0:
                monthly_needed = None
                status = "deadline_passed"
            else:
                months_remaining = max(days_until / 30.0, 0.1)
                monthly_needed = remaining / months_remaining
                status = "active"

        items.append(
            {
                "id": str(g.id),
                "title": g.title,
                "targetAmount": round(target, 2),
                "currentAmount": round(current, 2),
                "remainingAmount": round(remaining, 2),
                "progressPercentage": round(progress_pct, 1),
                "deadline": g.deadline.isoformat() if g.deadline else None,
                "daysUntilDeadline": days_until,
                "monthlyContributionNeeded": (
                    round(monthly_needed, 2) if monthly_needed is not None else None
                ),
                "status": status,
            }
        )

    items.sort(key=lambda x: x["progressPercentage"])

    result = {
        "goalCount": len(items),
        "goals": items,
    }

    log.info(
        "coach_tool_get_goals",
        user_id=str(user_id),
        count=len(items),
    )

    return result
