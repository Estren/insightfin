from __future__ import annotations

from typing import Literal, Optional
from uuid import UUID

from pydantic import BaseModel, Field


class AnalyzeRequest(BaseModel):
    userId: UUID
    type: Literal["MONTHLY_REPORT", "HEALTH_SCORE", "ALERT", "GOAL_PROJECTION"]
    month: Optional[str] = Field(None, pattern=r"^\d{4}-\d{2}$", examples=["2026-05"])
    force: bool = False


class AnalyzeResponse(BaseModel):
    accepted: bool = True
    userId: str
    type: str


class HealthResponse(BaseModel):
    status: str = "ok"
    service: str = "ai"
