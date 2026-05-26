from __future__ import annotations

from datetime import date, timedelta
from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def project_goal_completion(
    core_api: CoreApiClient,
    user_id: UUID,
    goal_title: str,
    monthly_contribution: float,
) -> dict:
    """Project when a goal completes given a monthly contribution rate.

    The agent matches `goal_title` against the user's goals (case-insensitive,
    fuzzy contains). Returns the estimated completion date and how it compares
    to the deadline. Requires a `monthly_contribution` because the core-api
    doesn't expose individual contribution history — the agent passes either
    the user's current pace (if known) or the value they want to simulate.
    """
    if monthly_contribution <= 0:
        return {"error": "monthly_contribution must be positive"}

    goals = await core_api.get_goals(user_id)
    needle = goal_title.lower().strip()
    match = next((g for g in goals if needle in g.title.lower()), None)
    if match is None:
        return {
            "error": f"no goal found matching '{goal_title}'",
            "availableTitles": [g.title for g in goals],
        }

    target = float(match.targetAmount)
    current = float(match.currentAmount)
    remaining = target - current

    if remaining <= 0:
        return {
            "title": match.title,
            "alreadyComplete": True,
            "currentAmount": round(current, 2),
            "targetAmount": round(target, 2),
        }

    months_to_complete = remaining / monthly_contribution
    days_to_complete = int(months_to_complete * 30)
    projected_completion = (date.today() + timedelta(days=days_to_complete)).isoformat()

    deadline_status = "no_deadline"
    days_vs_deadline = None
    if match.deadline:
        deadline = match.deadline
        days_vs_deadline = (deadline - (date.today() + timedelta(days=days_to_complete))).days
        if days_vs_deadline > 0:
            deadline_status = "ahead_of_deadline"
        elif days_vs_deadline == 0:
            deadline_status = "exactly_on_deadline"
        else:
            deadline_status = "after_deadline"

    result = {
        "title": match.title,
        "currentAmount": round(current, 2),
        "targetAmount": round(target, 2),
        "remainingAmount": round(remaining, 2),
        "monthlyContribution": round(monthly_contribution, 2),
        "monthsToComplete": round(months_to_complete, 1),
        "projectedCompletionDate": projected_completion,
        "deadline": match.deadline.isoformat() if match.deadline else None,
        "deadlineStatus": deadline_status,
        "daysVsDeadline": days_vs_deadline,
    }

    log.info(
        "coach_tool_project_goal_completion",
        user_id=str(user_id),
        goal=match.title,
        months_to_complete=result["monthsToComplete"],
        deadline_status=deadline_status,
    )

    return result
