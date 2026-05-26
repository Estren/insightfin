from __future__ import annotations

from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def get_budget_status(core_api: CoreApiClient, user_id: UUID, month: str) -> dict:
    """Per-budget usage tracking for `month`.

    Returns each budget with amount, spent, percentage, remaining, and a
    status flag (within / near_limit / over). Sorted by percentage used
    descending so the most-stretched budgets surface first. The summary
    fields (`overCount`, `nearLimitCount`) let the agent answer
    "how many budgets am I over?" without scanning the list.
    """
    budgets = await core_api.get_budgets(user_id, month)

    items: list[dict] = []
    for b in budgets:
        pct = float(b.percentageUsed)
        budget_amount = float(b.budgetAmount)
        spent_amount = float(b.spentAmount)

        if pct > 100:
            status = "over"
        elif pct >= 80:
            status = "near_limit"
        else:
            status = "within"

        items.append(
            {
                "category": b.categoryName,
                "budgetAmount": round(budget_amount, 2),
                "spentAmount": round(spent_amount, 2),
                "percentageUsed": round(pct, 1),
                "remaining": round(budget_amount - spent_amount, 2),
                "status": status,
            }
        )

    items.sort(key=lambda x: x["percentageUsed"], reverse=True)

    over_count = sum(1 for item in items if item["status"] == "over")
    near_count = sum(1 for item in items if item["status"] == "near_limit")

    result = {
        "month": month,
        "budgetCount": len(items),
        "overCount": over_count,
        "nearLimitCount": near_count,
        "budgets": items,
    }

    log.info(
        "coach_tool_get_budget_status",
        user_id=str(user_id),
        month=month,
        count=len(items),
        over=over_count,
        near_limit=near_count,
    )

    return result
