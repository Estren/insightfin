"""FastAPI route for the Coach Agent.

Exposes a synchronous `POST /coach/chat`. The agent itself is a singleton
provisioned in `app/main.py` lifespan and stashed in `app.state.coach_agent`.
When the Foundry endpoint isn't configured, the route still mounts but
returns 503 — this lets the rest of the service (batch orchestrator,
metrics, health) keep working in environments without Foundry access.
"""

from __future__ import annotations

from uuid import UUID

import structlog
from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel, Field

log = structlog.get_logger(__name__)

router = APIRouter(prefix="/coach", tags=["coach"])


class CoachChatRequest(BaseModel):
    userId: UUID
    question: str = Field(min_length=1, max_length=1000)


class CoachChatResponse(BaseModel):
    userId: UUID
    answer: str


@router.post("/chat", response_model=CoachChatResponse)
async def chat(body: CoachChatRequest, request: Request) -> CoachChatResponse:
    coach_agent = getattr(request.app.state, "coach_agent", None)
    if coach_agent is None:
        raise HTTPException(
            status_code=503,
            detail=(
                "Coach Agent is not available. Either the endpoint is not "
                "configured (AZURE_FOUNDRY_PROJECT_ENDPOINT missing) or the "
                "agent failed to initialize on startup — check server logs "
                "for `coach_agent_init_failed`."
            ),
        )

    try:
        answer = await coach_agent.ask(body.userId, body.question)
    except Exception as exc:
        log.error(
            "coach_chat_failed",
            user_id=str(body.userId),
            question=body.question,
            error=str(exc),
        )
        raise HTTPException(status_code=500, detail="Coach Agent failed to respond.")

    return CoachChatResponse(userId=body.userId, answer=answer)
