from __future__ import annotations

from uuid import UUID

import structlog

from app.coach_agent.tools.get_transactions import get_transactions
from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def compare_months(
    core_api: CoreApiClient,
    user_id: UUID,
    current_month: str,
    previous_month: str,
) -> dict:
    """Compute deltas between two months.

    Calls `get_transactions` for both months, then returns income/expense/
    balance deltas plus a per-category breakdown of categories that grew or
    shrank, sorted by absolute delta (largest moves first).

    The agent uses this for "como estou esse mês vs mês passado" without
    having to call `get_transactions` twice and reason about the deltas in
    natural language.
    """
    current = await get_transactions(core_api, user_id, current_month)
    previous = await get_transactions(core_api, user_id, previous_month)

    income_delta = current["totalIncome"] - previous["totalIncome"]
    expenses_delta = current["totalExpenses"] - previous["totalExpenses"]
    balance_delta = current["balance"] - previous["balance"]

    prev_by_cat = {
        (item["category"], item["type"]): item["total"]
        for item in previous["byCategory"]
    }
    curr_by_cat = {
        (item["category"], item["type"]): item["total"]
        for item in current["byCategory"]
    }

    all_keys = set(prev_by_cat) | set(curr_by_cat)
    category_deltas: list[dict] = []
    for key in all_keys:
        prev_total = prev_by_cat.get(key, 0.0)
        curr_total = curr_by_cat.get(key, 0.0)
        delta = curr_total - prev_total
        if delta == 0:
            continue
        category_deltas.append(
            {
                "category": key[0],
                "type": key[1],
                "previous": round(prev_total, 2),
                "current": round(curr_total, 2),
                "delta": round(delta, 2),
            }
        )

    category_deltas.sort(key=lambda x: abs(x["delta"]), reverse=True)

    result = {
        "currentMonth": current_month,
        "previousMonth": previous_month,
        "totals": {
            "incomeDelta": round(income_delta, 2),
            "expensesDelta": round(expenses_delta, 2),
            "balanceDelta": round(balance_delta, 2),
            "currentBalance": current["balance"],
            "previousBalance": previous["balance"],
        },
        "categoryDeltas": category_deltas,
    }

    log.info(
        "coach_tool_compare_months",
        user_id=str(user_id),
        current_month=current_month,
        previous_month=previous_month,
        balance_delta=result["totals"]["balanceDelta"],
    )

    return result
