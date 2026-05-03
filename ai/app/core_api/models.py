from __future__ import annotations

from datetime import date
from decimal import Decimal
from typing import Optional
from uuid import UUID

from pydantic import BaseModel


class UserSummary(BaseModel):
    id: UUID


class CategoryResponse(BaseModel):
    id: UUID
    name: str
    color: Optional[str] = None
    icon: Optional[str] = None


class TransactionResponse(BaseModel):
    id: UUID
    categoryId: UUID
    amount: Decimal
    type: str
    description: Optional[str] = None
    date: date


class BudgetStatusResponse(BaseModel):
    budgetId: UUID
    categoryId: UUID
    categoryName: str
    budgetAmount: Decimal
    spentAmount: Decimal
    percentageUsed: float


class GoalResponse(BaseModel):
    id: UUID
    title: str
    targetAmount: Decimal
    currentAmount: Decimal
    deadline: Optional[date] = None
    monthlyContribution: Optional[Decimal] = None
