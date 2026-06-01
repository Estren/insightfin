"""FastAPI routes for the Coach Agent.

Two routes:
- ``POST /coach/chat`` — synchronous, returns the full answer as JSON.
  Kept for the standalone test script and for the (legacy) non-streaming
  Quarkus proxy flow.
- ``POST /coach/chat/stream`` — Server-Sent Events. The Quarkus core-api
  proxies this to the authenticated frontend client.

The agent itself is a singleton provisioned in `app/main.py` lifespan and
stashed in `app.state.coach_agent`. When the Foundry endpoint isn't
configured, both routes return 503 so the rest of the service (batch
orchestrator, metrics, health) keeps working unaffected.
"""

from __future__ import annotations

import json
from uuid import UUID

import structlog
from fastapi import APIRouter, HTTPException, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from app.coach_agent.api.throttle import check_quota

log = structlog.get_logger(__name__)

router = APIRouter(prefix="/coach", tags=["coach"])

SSE_HEADERS = {
    "Cache-Control": "no-cache",
    "Connection": "keep-alive",
    # Disable any reverse-proxy buffering (nginx, Quarkus, etc.)
    "X-Accel-Buffering": "no",
}


class CoachChatRequest(BaseModel):
    userId: UUID
    question: str = Field(min_length=1, max_length=1000)
    threadId: str | None = None


class CoachChatResponse(BaseModel):
    userId: UUID
    answer: str


class CreateThreadResponse(BaseModel):
    threadId: str


def _require_coach_agent(request: Request):
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
    return coach_agent


@router.post("/threads", response_model=CreateThreadResponse, status_code=201)
async def create_thread(request: Request) -> CreateThreadResponse:
    """Create an empty Foundry thread. Core-api persists the returned id."""
    coach_agent = _require_coach_agent(request)
    thread_id = await coach_agent.create_thread()
    return CreateThreadResponse(threadId=thread_id)


@router.get("/threads/{thread_id}/messages")
async def thread_messages(thread_id: str, request: Request) -> list[dict]:
    """Hydrate a thread's full message history (chronological)."""
    coach_agent = _require_coach_agent(request)
    try:
        return await coach_agent.list_messages(thread_id)
    except Exception as exc:
        log.error("coach_list_messages_failed", thread_id=thread_id, error=str(exc))
        raise HTTPException(status_code=502, detail="Failed to load thread messages.")


def _enforce_quota(user_id: UUID) -> None:
    allowed, retry_after = check_quota(user_id)
    if not allowed:
        log.warning("coach_rate_limited", user_id=str(user_id), retry_after=retry_after)
        raise HTTPException(
            status_code=429,
            detail="Coach Agent rate limit exceeded for this user.",
            headers={"Retry-After": str(retry_after)},
        )


@router.post("/chat", response_model=CoachChatResponse)
async def chat(body: CoachChatRequest, request: Request) -> CoachChatResponse:
    coach_agent = _require_coach_agent(request)
    _enforce_quota(body.userId)
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


@router.post("/chat/stream")
async def chat_stream(body: CoachChatRequest, request: Request) -> StreamingResponse:
    """Stream the agent's response as Server-Sent Events.

    Each event has the shape:
        event: <type>
        data: <json payload>

    Types: ``token``, ``tool_call``, ``citation``, ``error``, ``done``.
    A consumer that joins all ``token.data`` in order reconstructs the
    final assistant message.
    """
    coach_agent = _require_coach_agent(request)
    _enforce_quota(body.userId)

    async def event_generator():
        try:
            async for event in coach_agent.ask_stream(
                body.userId, body.question, thread_id=body.threadId
            ):
                payload = {k: v for k, v in event.items() if k != "type"}
                yield (
                    f"event: {event['type']}\n"
                    f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"
                )
        except Exception as exc:
            log.error(
                "coach_chat_stream_failed",
                user_id=str(body.userId),
                error=str(exc),
            )
            yield (
                "event: error\n"
                f"data: {json.dumps({'data': str(exc)}, ensure_ascii=False)}\n\n"
            )

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers=SSE_HEADERS,
    )
