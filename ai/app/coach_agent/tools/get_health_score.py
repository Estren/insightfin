from __future__ import annotations

from uuid import UUID

import structlog

from app.agent.context_builder import _build_health_score
from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)


async def get_health_score(core_api: CoreApiClient, user_id: UUID, month: str) -> dict:
    """Compute the user's health score for `month`.

    Reuses `_build_health_score` from the batch orchestrator so the coach and
    the dashboard gauge always agree on the number. Falls back to an empty
    score (50/100 across the board) when core-api returns no data for the
    period — `_build_health_score` is defensive on missing inputs.
    """
    context = await core_api.get_user_context(user_id, month)
    result = _build_health_score(context, month)
    metadata = result["computed_metadata"]

    log.info(
        "coach_tool_get_health_score",
        user_id=str(user_id),
        month=month,
        score=metadata["score"],
    )

    return {
        "month": month,
        "score": metadata["score"],
        "breakdown": metadata["breakdown"],
    }
