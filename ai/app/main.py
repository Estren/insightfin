from __future__ import annotations

from contextlib import asynccontextmanager

import structlog
from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator

from app.api.routes import router
from app.agent.llm_client import LLMClient
from app.agent.orchestrator import Orchestrator
from app.core_api.client import CoreApiClient, make_http_client
from app.kafka.consumer import KafkaConsumer
from app.scheduler.jobs import Scheduler

structlog.configure(
    processors=[
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
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
    app = FastAPI(title="Orizon AI Service", version="1.0.0", lifespan=lifespan)
    app.include_router(router)
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")
    return app


app = create_app()
