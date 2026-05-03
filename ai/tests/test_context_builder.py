from __future__ import annotations

from datetime import date
from decimal import Decimal
from uuid import uuid4

import pytest

from app.agent import context_builder
from app.core_api.models import BudgetStatusResponse, CategoryResponse, GoalResponse, TransactionResponse


# ---------------------------------------------------------------------------
# Fixtures / helpers
# ---------------------------------------------------------------------------

def txn(type_: str, amount: float, category_id=None) -> TransactionResponse:
    return TransactionResponse(
        id=uuid4(),
        categoryId=category_id or uuid4(),
        amount=Decimal(str(amount)),
        type=type_,
        date=date(2026, 5, 15),
    )


def budget(name: str, limit: float, spent: float) -> BudgetStatusResponse:
    pct = spent / limit * 100 if limit > 0 else 0.0
    return BudgetStatusResponse(
        budgetId=uuid4(),
        categoryId=uuid4(),
        categoryName=name,
        budgetAmount=Decimal(str(limit)),
        spentAmount=Decimal(str(spent)),
        percentageUsed=pct,
    )


def goal(title: str, target: float, current: float, deadline=None, monthly=None) -> GoalResponse:
    return GoalResponse(
        id=uuid4(),
        title=title,
        targetAmount=Decimal(str(target)),
        currentAmount=Decimal(str(current)),
        deadline=deadline,
        monthlyContribution=Decimal(str(monthly)) if monthly is not None else None,
    )


def empty_context() -> dict:
    return {"transactions": [], "budgets": [], "goals": [], "categories": []}


# ---------------------------------------------------------------------------
# ALERT
# ---------------------------------------------------------------------------

class TestBuildAlert:
    def test_returns_none_when_no_budget_critical(self):
        ctx = {**empty_context(), "budgets": [budget("Food", 1000, 500)]}  # 50%
        assert context_builder.build("ALERT", ctx, "2026-05") is None

    def test_returns_none_when_budgets_empty(self):
        assert context_builder.build("ALERT", empty_context(), "2026-05") is None

    def test_warning_when_between_80_and_100(self):
        ctx = {**empty_context(), "budgets": [budget("Food", 1000, 850)]}  # 85%
        result = context_builder.build("ALERT", ctx, "2026-05")
        assert result is not None
        assert result["computed_metadata"]["alertType"] == "BUDGET_WARNING"
        assert result["computed_metadata"]["percentageUsed"] == 85.0

    def test_exceeded_when_above_100(self):
        ctx = {**empty_context(), "budgets": [budget("Food", 1000, 1100)]}  # 110%
        result = context_builder.build("ALERT", ctx, "2026-05")
        assert result["computed_metadata"]["alertType"] == "BUDGET_EXCEEDED"

    def test_picks_worst_budget(self):
        ctx = {
            **empty_context(),
            "budgets": [
                budget("Food", 1000, 820),   # 82%
                budget("Travel", 500, 490),  # 98%
            ],
        }
        result = context_builder.build("ALERT", ctx, "2026-05")
        assert result["computed_metadata"]["categoryName"] == "Travel"

    def test_metadata_amounts_are_rounded(self):
        ctx = {**empty_context(), "budgets": [budget("Food", 1000, 900)]}
        meta = context_builder.build("ALERT", ctx, "2026-05")["computed_metadata"]
        assert meta["budgetAmount"] == 1000.0
        assert meta["spentAmount"] == 900.0


# ---------------------------------------------------------------------------
# MONTHLY_REPORT
# ---------------------------------------------------------------------------

class TestBuildMonthlyReport:
    def test_calculates_income_expense_balance(self):
        ctx = {
            **empty_context(),
            "transactions": [txn("INCOME", 5000), txn("EXPENSE", 3200)],
        }
        meta = context_builder.build("MONTHLY_REPORT", ctx, "2026-05")["computed_metadata"]
        assert meta["totalIncome"] == 5000.0
        assert meta["totalExpenses"] == 3200.0
        assert meta["balance"] == 1800.0

    def test_top_categories_sorted_by_amount(self):
        cat_id = uuid4()
        cat_id2 = uuid4()
        cats = [
            CategoryResponse(id=cat_id, name="Food"),
            CategoryResponse(id=cat_id2, name="Transport"),
        ]
        ctx = {
            **empty_context(),
            "transactions": [
                txn("EXPENSE", 200, cat_id),
                txn("EXPENSE", 800, cat_id),   # Food total: 1000
                txn("EXPENSE", 300, cat_id2),  # Transport total: 300
            ],
            "categories": cats,
        }
        top = context_builder.build("MONTHLY_REPORT", ctx, "2026-05")["computed_metadata"]["topCategories"]
        assert top[0]["name"] == "Food"
        assert top[0]["amount"] == 1000.0
        assert top[1]["name"] == "Transport"

    def test_empty_transactions_yields_zeros(self):
        meta = context_builder.build("MONTHLY_REPORT", empty_context(), "2026-05")["computed_metadata"]
        assert meta["totalIncome"] == 0.0
        assert meta["totalExpenses"] == 0.0
        assert meta["balance"] == 0.0
        assert meta["topCategories"] == []

    def test_category_percentage_sums_correctly(self):
        cat_id = uuid4()
        cats = [CategoryResponse(id=cat_id, name="Food")]
        ctx = {
            **empty_context(),
            "transactions": [txn("EXPENSE", 500, cat_id), txn("EXPENSE", 500, cat_id)],
            "categories": cats,
        }
        top = context_builder.build("MONTHLY_REPORT", ctx, "2026-05")["computed_metadata"]["topCategories"]
        assert top[0]["percentage"] == 100.0


# ---------------------------------------------------------------------------
# HEALTH_SCORE
# ---------------------------------------------------------------------------

class TestBuildHealthScore:
    def test_perfect_savings_yields_100_savings_score(self):
        # 50% savings rate → score = 100
        ctx = {
            **empty_context(),
            "transactions": [txn("INCOME", 1000), txn("EXPENSE", 500)],
        }
        breakdown = context_builder.build("HEALTH_SCORE", ctx, "2026-05")["computed_metadata"]["breakdown"]
        assert breakdown["savingsRate"] == 100

    def test_zero_savings_yields_zero_savings_score(self):
        ctx = {
            **empty_context(),
            "transactions": [txn("INCOME", 1000), txn("EXPENSE", 1000)],
        }
        breakdown = context_builder.build("HEALTH_SCORE", ctx, "2026-05")["computed_metadata"]["breakdown"]
        assert breakdown["savingsRate"] == 0

    def test_all_budgets_within_limit_yields_100_budget_score(self):
        ctx = {
            **empty_context(),
            "budgets": [budget("Food", 1000, 500), budget("Transport", 500, 400)],
        }
        breakdown = context_builder.build("HEALTH_SCORE", ctx, "2026-05")["computed_metadata"]["breakdown"]
        assert breakdown["budgetAdherence"] == 100

    def test_no_budgets_defaults_budget_score_to_50(self):
        breakdown = context_builder.build("HEALTH_SCORE", empty_context(), "2026-05")["computed_metadata"]["breakdown"]
        assert breakdown["budgetAdherence"] == 50

    def test_no_goals_defaults_goal_score_to_50(self):
        breakdown = context_builder.build("HEALTH_SCORE", empty_context(), "2026-05")["computed_metadata"]["breakdown"]
        assert breakdown["goalProgress"] == 50

    def test_overall_score_is_average_of_four_components(self):
        result = context_builder.build("HEALTH_SCORE", empty_context(), "2026-05")
        meta = result["computed_metadata"]
        expected = int((
            meta["breakdown"]["savingsRate"]
            + meta["breakdown"]["budgetAdherence"]
            + meta["breakdown"]["goalProgress"]
            + meta["breakdown"]["expenseConsistency"]
        ) / 4)
        assert meta["score"] == expected


# ---------------------------------------------------------------------------
# GOAL_PROJECTION
# ---------------------------------------------------------------------------

class TestBuildGoalProjection:
    def test_returns_none_when_no_goals(self):
        assert context_builder.build("GOAL_PROJECTION", empty_context(), "2026-05") is None

    def test_on_track_when_monthly_exceeds_required(self):
        # target=1200, current=0, remaining=1200
        # deadline in 12 months → required=100/month; monthly=200 → on_track=True
        deadline = date(2027, 5, 1)
        ctx = {**empty_context(), "goals": [goal("Trip", 1200, 0, deadline=deadline, monthly=200)]}
        meta = context_builder.build("GOAL_PROJECTION", ctx, "2026-05")["computed_metadata"]
        assert meta["onTrack"] is True

    def test_not_on_track_when_monthly_insufficient(self):
        deadline = date(2027, 5, 1)
        ctx = {**empty_context(), "goals": [goal("Trip", 1200, 0, deadline=deadline, monthly=50)]}
        meta = context_builder.build("GOAL_PROJECTION", ctx, "2026-05")["computed_metadata"]
        assert meta["onTrack"] is False

    def test_no_deadline_always_on_track(self):
        ctx = {**empty_context(), "goals": [goal("Trip", 1000, 0)]}
        meta = context_builder.build("GOAL_PROJECTION", ctx, "2026-05")["computed_metadata"]
        assert meta["onTrack"] is True

    def test_picks_goal_with_most_remaining(self):
        # Two goals: one 90% done, one 10% done → picks the 10% done one
        ctx = {
            **empty_context(),
            "goals": [
                goal("Almost done", 1000, 900),  # 90% done
                goal("Just started", 1000, 100), # 10% done → highest priority
            ],
        }
        meta = context_builder.build("GOAL_PROJECTION", ctx, "2026-05")["computed_metadata"]
        assert meta["goalTitle"] == "Just started"

    def test_metadata_contains_required_fields(self):
        ctx = {**empty_context(), "goals": [goal("Trip", 1000, 300, monthly=100)]}
        meta = context_builder.build("GOAL_PROJECTION", ctx, "2026-05")["computed_metadata"]
        assert "goalTitle" in meta
        assert "targetAmount" in meta
        assert "currentAmount" in meta
        assert "onTrack" in meta
