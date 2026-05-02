from __future__ import annotations

from datetime import date
from typing import Optional
from uuid import UUID

from app.core_api.models import BudgetStatusResponse, CategoryResponse, GoalResponse, TransactionResponse


def build(analysis_type: str, context: dict, month: str) -> Optional[dict]:
    """
    Returns {"computed_metadata": {...}, "prompt_context": str} or None if analysis should be skipped
    (e.g., no budget over threshold for ALERT, no goals for GOAL_PROJECTION).
    """
    builders = {
        "MONTHLY_REPORT": _build_monthly_report,
        "HEALTH_SCORE": _build_health_score,
        "ALERT": _build_alert,
        "GOAL_PROJECTION": _build_goal_projection,
    }
    return builders[analysis_type](context, month)


def _category_name(category_id: UUID, categories: list[CategoryResponse]) -> str:
    for cat in categories:
        if cat.id == category_id:
            return cat.name
    return "Outros"


def _build_monthly_report(context: dict, month: str) -> dict:
    transactions: list[TransactionResponse] = context["transactions"]
    budgets: list[BudgetStatusResponse] = context["budgets"]
    categories: list[CategoryResponse] = context["categories"]

    income_txns = [t for t in transactions if t.type.upper() == "INCOME"]
    expense_txns = [t for t in transactions if t.type.upper() == "EXPENSE"]

    total_income = float(sum(t.amount for t in income_txns))
    total_expenses = float(sum(t.amount for t in expense_txns))
    balance = total_income - total_expenses

    category_totals: dict[str, float] = {}
    for txn in expense_txns:
        name = _category_name(txn.categoryId, categories)
        category_totals[name] = category_totals.get(name, 0.0) + float(txn.amount)

    top_categories = sorted(
        [
            {
                "name": k,
                "amount": round(v, 2),
                "percentage": round(v / total_expenses * 100, 1) if total_expenses > 0 else 0.0,
            }
            for k, v in category_totals.items()
        ],
        key=lambda x: x["amount"],
        reverse=True,
    )[:5]

    # highlights will be filled by LLM
    computed_metadata = {
        "totalIncome": round(total_income, 2),
        "totalExpenses": round(total_expenses, 2),
        "balance": round(balance, 2),
        "topCategories": top_categories,
        "highlights": [],
    }

    budget_lines = "\n".join(
        f"- {b.categoryName}: R$ {float(b.spentAmount):.2f} of R$ {float(b.budgetAmount):.2f} ({b.percentageUsed:.0f}%)"
        for b in budgets
    ) or "No budgets configured."

    cat_lines = "\n".join(
        f"- {c['name']}: R$ {c['amount']:.2f} ({c['percentage']:.1f}%)"
        for c in top_categories
    ) or "No expense categories."

    prompt_context = (
        f"Month: {month}\n"
        f"Income: R$ {total_income:.2f} ({len(income_txns)} transactions)\n"
        f"Expenses: R$ {total_expenses:.2f} ({len(expense_txns)} transactions)\n"
        f"Balance: R$ {balance:.2f}\n\n"
        f"Top spending categories:\n{cat_lines}\n\n"
        f"Budget status:\n{budget_lines}"
    )

    return {"computed_metadata": computed_metadata, "prompt_context": prompt_context}


def _build_health_score(context: dict, month: str) -> dict:
    transactions: list[TransactionResponse] = context["transactions"]
    budgets: list[BudgetStatusResponse] = context["budgets"]
    goals: list[GoalResponse] = context["goals"]

    income_txns = [t for t in transactions if t.type.upper() == "INCOME"]
    expense_txns = [t for t in transactions if t.type.upper() == "EXPENSE"]

    total_income = float(sum(t.amount for t in income_txns))
    total_expenses = float(sum(t.amount for t in expense_txns))
    savings_rate = (total_income - total_expenses) / total_income * 100 if total_income > 0 else 0.0

    # Savings rate score: 50% savings = 100 points, linear
    savings_score = max(0, min(100, int(savings_rate * 2)))

    # Budget adherence: % of budgets within limit
    if budgets:
        within = sum(1 for b in budgets if b.percentageUsed <= 100)
        budget_score = int(within / len(budgets) * 100)
    else:
        budget_score = 50

    # Goal progress: average completion %
    if goals:
        progresses = [
            min(100.0, float(g.currentAmount) / float(g.targetAmount) * 100)
            for g in goals
            if float(g.targetAmount) > 0
        ]
        goal_score = int(sum(progresses) / len(progresses)) if progresses else 50
    else:
        goal_score = 50

    # Expense consistency: low coefficient of variation = consistent spending
    if expense_txns:
        amounts = [float(t.amount) for t in expense_txns]
        mean = sum(amounts) / len(amounts)
        variance = sum((a - mean) ** 2 for a in amounts) / len(amounts)
        std_dev = variance ** 0.5
        cv = std_dev / mean if mean > 0 else 0.0
        consistency_score = max(0, min(100, int((1 - min(cv, 1)) * 100)))
    else:
        consistency_score = 50

    overall = int((savings_score + budget_score + goal_score + consistency_score) / 4)

    computed_metadata = {
        "score": overall,
        "breakdown": {
            "savingsRate": savings_score,
            "budgetAdherence": budget_score,
            "goalProgress": goal_score,
            "expenseConsistency": consistency_score,
        },
    }

    budgets_within = sum(1 for b in budgets if b.percentageUsed <= 100)

    prompt_context = (
        f"Month: {month}\n"
        f"Income: R$ {total_income:.2f} | Expenses: R$ {total_expenses:.2f}\n"
        f"Savings rate: {savings_rate:.1f}%\n\n"
        f"Financial health score: {overall}/100\n"
        f"- Savings rate score: {savings_score}/100\n"
        f"- Budget adherence score: {budget_score}/100 ({budgets_within}/{len(budgets)} budgets within limit)\n"
        f"- Goal progress score: {goal_score}/100\n"
        f"- Expense consistency score: {consistency_score}/100"
    )

    return {"computed_metadata": computed_metadata, "prompt_context": prompt_context}


def _build_alert(context: dict, month: str) -> Optional[dict]:
    budgets: list[BudgetStatusResponse] = context["budgets"]

    critical = [b for b in budgets if b.percentageUsed >= 80.0]
    if not critical:
        return None

    worst = max(critical, key=lambda b: b.percentageUsed)
    alert_type = "BUDGET_EXCEEDED" if worst.percentageUsed >= 100.0 else "BUDGET_WARNING"

    computed_metadata = {
        "alertType": alert_type,
        "categoryName": worst.categoryName,
        "budgetAmount": round(float(worst.budgetAmount), 2),
        "spentAmount": round(float(worst.spentAmount), 2),
        "percentageUsed": round(worst.percentageUsed, 1),
    }

    prompt_context = (
        f"Month: {month}\n"
        f"Alert type: {alert_type}\n"
        f"Category: {worst.categoryName}\n"
        f"Budget limit: R$ {float(worst.budgetAmount):.2f}\n"
        f"Amount spent: R$ {float(worst.spentAmount):.2f}\n"
        f"Percentage used: {worst.percentageUsed:.1f}%"
    )

    return {"computed_metadata": computed_metadata, "prompt_context": prompt_context}


def _build_goal_projection(context: dict, month: str) -> Optional[dict]:
    goals: list[GoalResponse] = context["goals"]
    if not goals:
        return None

    def _priority(g: GoalResponse) -> float:
        if float(g.targetAmount) <= 0:
            return 0.0
        return 1.0 - min(1.0, float(g.currentAmount) / float(g.targetAmount))

    goal = max(goals, key=_priority)

    target = float(goal.targetAmount)
    current = float(goal.currentAmount)
    remaining = max(0.0, target - current)
    monthly = float(goal.monthlyContribution or 0)

    projected_date: Optional[str] = None
    if monthly > 0:
        months_needed = remaining / monthly
        today = date.today()
        total_months = today.month - 1 + int(months_needed)
        proj_year = today.year + total_months // 12
        proj_month = total_months % 12 + 1
        projected_date = f"{proj_year}-{proj_month:02d}"

    on_track = True
    required_monthly: Optional[float] = None
    if goal.deadline:
        today = date.today()
        months_to_deadline = (goal.deadline.year - today.year) * 12 + (goal.deadline.month - today.month)
        months_to_deadline = max(1, months_to_deadline)
        required_monthly = round(remaining / months_to_deadline, 2)
        on_track = monthly >= required_monthly if monthly > 0 else False

    computed_metadata = {
        "goalTitle": goal.title,
        "targetAmount": round(target, 2),
        "currentAmount": round(current, 2),
        "onTrack": on_track,
        "projectedCompletionDate": projected_date,
        "requiredMonthlySaving": required_monthly,
        "currentMonthlySaving": round(monthly, 2) if monthly > 0 else None,
    }

    deadline_str = goal.deadline.strftime("%Y-%m") if goal.deadline else "no deadline"
    progress_pct = current / target * 100 if target > 0 else 0.0
    req_line = f"\nRequired monthly to meet deadline: R$ {required_monthly:.2f}" if required_monthly else ""
    proj_line = f"\nProjected completion: {projected_date}" if projected_date else ""

    prompt_context = (
        f"Month: {month}\n"
        f"Goal: {goal.title}\n"
        f"Target: R$ {target:.2f} | Current: R$ {current:.2f} ({progress_pct:.1f}% achieved)\n"
        f"Remaining: R$ {remaining:.2f}\n"
        f"Monthly contribution: R$ {monthly:.2f}\n"
        f"Deadline: {deadline_str}\n"
        f"On track: {'Yes' if on_track else 'No'}"
        f"{req_line}{proj_line}"
    )

    return {"computed_metadata": computed_metadata, "prompt_context": prompt_context}
