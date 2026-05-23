from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Any

import sentry_sdk
import structlog
from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator

from app.api.routes import router
from app.agent.llm_client import LLMClient
from app.agent.orchestrator import Orchestrator
from app.config import settings
from app.core_api.client import CoreApiClient, make_http_client
from app.kafka.consumer import KafkaConsumer
from app.scheduler.jobs import Scheduler

if settings.sentry_dsn:
    sentry_sdk.init(
        dsn=settings.sentry_dsn,
        environment=settings.sentry_environment,
        # Errors only — no performance tracing, keeps telemetry volume low.
        traces_sample_rate=0.0,
    )


def _forward_errors_to_sentry(
    _logger: Any, _method_name: str, event_dict: dict[str, Any]
) -> dict[str, Any]:
    """structlog processor that mirrors error/critical logs into Sentry.

    structlog is configured with ``PrintLoggerFactory`` and bypasses stdlib
    logging, so Sentry's logging integration never sees these events — this
    bridges them explicitly. A no-op when Sentry is not initialised.
    """
    if event_dict.get("level") in ("error", "critical"):
        if event_dict.get("exc_info"):
            sentry_sdk.capture_exception()
        else:
            sentry_sdk.capture_message(str(event_dict.get("event", "")), level="error")
    return event_dict


structlog.configure(
    processors=[
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        _forward_errors_to_sentry,
        structlog.processors.JSONRenderer(),
    ],
    wrapper_class=structlog.make_filtering_bound_logger(20),  # INFO
    context_class=dict,
    logger_factory=structlog.PrintLoggerFactory(),
)

log = structlog.get_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    http_client = make_http_client()
    core_api = CoreApiClient(http_client)
    llm = LLMClient()
    orchestrator = Orchestrator(core_api, llm)
    kafka_consumer = KafkaConsumer(orchestrator)
    scheduler = Scheduler(orchestrator)

    app.state.orchestrator = orchestrator

    await kafka_consumer.start()
    scheduler.start()

    log.info("ai_service_started")
    yield

    scheduler.stop()
    await kafka_consumer.stop()
    await http_client.aclose()
    log.info("ai_service_stopped")


def create_app() -> FastAPI:
    app = FastAPI(title="insightfin AI Service", version="1.0.0", lifespan=lifespan)
    app.include_router(router)
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")
    return app


app = create_app()
