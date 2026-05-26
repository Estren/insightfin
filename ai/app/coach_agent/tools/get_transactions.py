from __future__ import annotations

from collections import defaultdict
from uuid import UUID

import structlog

from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def get_transactions(core_api: CoreApiClient, user_id: UUID, month: str) -> dict:
    """Aggregated transaction summary for `month`.

    Returns totals by type, transaction count, and a per-category breakdown
    sorted by total amount (largest first). Names are joined in from the
    user's categories list because `TransactionResponse` only carries the
    category id, not the name.

    The agent receives aggregates rather than raw transactions to keep the
    token cost predictable across months with many entries.
    """
    context = await core_api.get_user_context(user_id, month)
    transactions = context["transactions"]
    categories = context["categories"]

    cat_names = {c.id: c.name for c in categories}

    total_income = 0.0
    total_expenses = 0.0
    by_cat: dict[tuple[str, str], dict] = defaultdict(
        lambda: {"total": 0.0, "count": 0}
    )

    for t in transactions:
        amount = float(t.amount)
        type_upper = t.type.upper()
        cat_name = cat_names.get(t.categoryId, "Sem categoria")

        if type_upper == "INCOME":
            total_income += amount
        else:
            total_expenses += amount

        bucket = by_cat[(cat_name, type_upper)]
        bucket["total"] += amount
        bucket["count"] += 1

    by_category = sorted(
        [
            {
                "category": key[0],
                "type": key[1],
                "total": round(value["total"], 2),
                "count": value["count"],
            }
            for key, value in by_cat.items()
        ],
        key=lambda entry: entry["total"],
        reverse=True,
    )

    result = {
        "month": month,
        "totalIncome": round(total_income, 2),
        "totalExpenses": round(total_expenses, 2),
        "balance": round(total_income - total_expenses, 2),
        "transactionCount": len(transactions),
        "byCategory": by_category,
    }

    log.info(
        "coach_tool_get_transactions",
        user_id=str(user_id),
        month=month,
        count=len(transactions),
        income=result["totalIncome"],
        expenses=result["totalExpenses"],
    )

    return result
