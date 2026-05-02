from __future__ import annotations

import asyncio
import json
from uuid import UUID

import structlog
from aiokafka import AIOKafkaConsumer

from app.config import settings

log = structlog.get_logger(__name__)

TOPICS = ["transaction.created", "goal.contributed"]


class KafkaConsumer:
    def __init__(self, orchestrator) -> None:
        self._orchestrator = orchestrator
        self._consumer: AIOKafkaConsumer | None = None
        self._task: asyncio.Task | None = None

    async def start(self) -> None:
        self._consumer = AIOKafkaConsumer(
            *TOPICS,
            bootstrap_servers=settings.kafka_bootstrap_servers,
            group_id=settings.kafka_consumer_group,
            auto_offset_reset="latest",
            enable_auto_commit=True,
            value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        )
        await self._consumer.start()
        self._task = asyncio.create_task(self._consume_loop())
        log.info("kafka_consumer_started", topics=TOPICS)

    async def stop(self) -> None:
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        if self._consumer:
            await self._consumer.stop()
        log.info("kafka_consumer_stopped")

    async def _consume_loop(self) -> None:
        assert self._consumer is not None
        async for msg in self._consumer:
            try:
                await self._handle(msg.topic, msg.value)
            except Exception as exc:
                log.error("kafka_message_failed", topic=msg.topic, error=str(exc))

    async def _handle(self, topic: str, payload: dict) -> None:
        user_id = UUID(payload["userId"])

        if topic == "transaction.created":
            log.info("kafka_event_received", topic=topic, user_id=str(user_id))
            await self._orchestrator.analyze(user_id, "ALERT")

        elif topic == "goal.contributed":
            log.info("kafka_event_received", topic=topic, user_id=str(user_id))
            await self._orchestrator.analyze(user_id, "GOAL_PROJECTION")
