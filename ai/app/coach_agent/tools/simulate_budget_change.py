from __future__ import annotations

from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def simulate_budget_change(
    core_api: CoreApiClient,
    user_id: UUID,
    category: str,
    additional_amount: float,
    month: str,
) -> dict:
    """Simulate the impact of additional spending on a budget.

    Matches `category` against the user's budgets for `month` (case-insensitive
    contains). Returns the current vs simulated state side-by-side with a
    status flag. The agent uses this for "posso gastar mais R$X em Y?"
    without computing the math itself. `additional_amount` is the extra spend
    to layer on top of what's already been spent — pass a positive number.
    """
    if additional_amount < 0:
        return {"error": "additional_amount must be non-negative"}

    budgets = await core_api.get_budgets(user_id, month)
    needle = category.lower().strip()
    match = next((b for b in budgets if needle in b.categoryName.lower()), None)
    if match is None:
        return {
            "error": f"no budget found for category '{category}' in {month}",
            "availableCategories": [b.categoryName for b in budgets],
        }

    budget_amount = float(match.budgetAmount)
    current_spent = float(match.spentAmount)
    simulated_spent = current_spent + additional_amount
    simulated_pct = (simulated_spent / budget_amount * 100) if budget_amount > 0 else 0.0
    simulated_remaining = budget_amount - simulated_spent

    if simulated_pct > 100:
        status = "would_overflow"
    elif simulated_pct >= 80:
        status = "would_be_near_limit"
    else:
        status = "would_stay_within"

    result = {
        "category": match.categoryName,
        "month": month,
        "budgetAmount": round(budget_amount, 2),
        "currentSpent": round(current_spent, 2),
        "currentPercentage": round(float(match.percentageUsed), 1),
        "additionalAmount": round(additional_amount, 2),
        "simulatedSpent": round(simulated_spent, 2),
        "simulatedPercentage": round(simulated_pct, 1),
        "simulatedRemaining": round(simulated_remaining, 2),
        "status": status,
    }

    log.info(
        "coach_tool_simulate_budget_change",
        user_id=str(user_id),
        category=match.categoryName,
        month=month,
        additional=additional_amount,
        status=status,
    )

    return result
