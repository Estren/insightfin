from __future__ import annotations

import json
import time
from datetime import date
from typing import Any

import structlog
from openai import AsyncAzureOpenAI, APIConnectionError, APITimeoutError, RateLimitError
from tenacity import retry, retry_if_exception_type, stop_after_attempt, wait_exponential

from app.config import settings

log = structlog.get_logger(__name__)

# In-memory daily call counter (resets on service restart / date change)
_daily_count = 0
_daily_date = date.today()


class LLMDailyLimitError(Exception):
    pass


def _check_and_increment() -> None:
    global _daily_count, _daily_date
    today = date.today()
    if today != _daily_date:
        _daily_count = 0
        _daily_date = today
    if _daily_count >= settings.max_llm_calls_per_day:
        raise LLMDailyLimitError(
            f"Daily LLM call limit reached ({settings.max_llm_calls_per_day})"
        )
    _daily_count += 1


class LLMClient:
    def __init__(self) -> None:
        self._client = AsyncAzureOpenAI(
            azure_endpoint=settings.azure_openai_endpoint,
            api_key=settings.azure_openai_key,
            api_version=settings.azure_openai_api_version,
        )

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=30),
        retry=retry_if_exception_type((RateLimitError, APIConnectionError, APITimeoutError)),
        reraise=True,
    )
    async def generate(self, system_prompt: str, user_message: str) -> dict[str, Any]:
        _check_and_increment()

        start = time.monotonic()
        response = await self._client.chat.completions.create(
            model=settings.azure_openai_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_message},
            ],
            response_format={"type": "json_object"},
            timeout=settings.llm_timeout_seconds,
        )
        duration_ms = int((time.monotonic() - start) * 1000)

        usage = response.usage
        log.info(
            "llm_called",
            tokens_prompt=usage.prompt_tokens if usage else None,
            tokens_completion=usage.completion_tokens if usage else None,
            duration_ms=duration_ms,
        )

        raw = response.choices[0].message.content or "{}"
        return json.loads(raw)
