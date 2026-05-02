from __future__ import annotations

import asyncio

import structlog
from fastapi import APIRouter, BackgroundTasks, HTTPException, Request

from app.api.schemas import AnalyzeRequest, AnalyzeResponse, HealthResponse

log = structlog.get_logger(__name__)

router = APIRouter()


@router.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    return HealthResponse()


@router.post("/analyze", response_model=AnalyzeResponse, status_code=202)
async def analyze(body: AnalyzeRequest, background_tasks: BackgroundTasks, request: Request) -> AnalyzeResponse:
    orchestrator = request.app.state.orchestrator

    background_tasks.add_task(
        _run_analysis,
        orchestrator,
        body.userId,
        body.type,
        body.month,
        body.force,
    )

    return AnalyzeResponse(userId=str(body.userId), type=body.type)


async def _run_analysis(orchestrator, user_id, analysis_type, month, force) -> None:
    try:
        await orchestrator.analyze(user_id, analysis_type, month=month, force=force)
    except Exception as exc:
        log.error("analyze_endpoint_error", user_id=str(user_id), type=analysis_type, error=str(exc))
