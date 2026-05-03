from __future__ import annotations

from datetime import date
from unittest.mock import AsyncMock, MagicMock

import pytest

import app.agent.llm_client as llm_module
from app.agent.llm_client import LLMClient, LLMDailyLimitError, _check_and_increment


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(autouse=True)
def reset_daily_counter(monkeypatch):
    monkeypatch.setattr(llm_module, "_daily_count", 0)
    monkeypatch.setattr(llm_module, "_daily_date", date.today())


# ---------------------------------------------------------------------------
# Daily limit counter
# ---------------------------------------------------------------------------

class TestDailyLimitCounter:
    def test_increments_on_each_call(self):
        _check_and_increment()
        assert llm_module._daily_count == 1
        _check_and_increment()
        assert llm_module._daily_count == 2

    def test_raises_when_limit_reached(self, monkeypatch):
        monkeypatch.setattr(llm_module, "_daily_count", llm_module.settings.max_llm_calls_per_day)
        with pytest.raises(LLMDailyLimitError):
            _check_and_increment()

    def test_resets_on_new_day(self, monkeypatch):
        monkeypatch.setattr(llm_module, "_daily_count", llm_module.settings.max_llm_calls_per_day)
        monkeypatch.setattr(llm_module, "_daily_date", date(2000, 1, 1))  # yesterday
        _check_and_increment()  # should not raise — new day resets counter
        assert llm_module._daily_count == 1


# ---------------------------------------------------------------------------
# LLMClient.generate
# ---------------------------------------------------------------------------

class TestLLMClientGenerate:
    async def test_parses_json_response(self, mocker):
        mock_openai = MagicMock()
        mock_openai.chat.completions.create = AsyncMock(
            return_value=MagicMock(
                choices=[MagicMock(message=MagicMock(content='{"title": "T", "content": "C"}'))],
                usage=MagicMock(prompt_tokens=10, completion_tokens=5),
            )
        )
        mocker.patch("app.agent.llm_client.AsyncOpenAI", return_value=mock_openai)

        client = LLMClient()
        result = await client.generate("system", "user")

        assert result == {"title": "T", "content": "C"}

    async def test_raises_daily_limit_before_calling_api(self, mocker, monkeypatch):
        monkeypatch.setattr(llm_module, "_daily_count", llm_module.settings.max_llm_calls_per_day)

        mock_openai = MagicMock()
        mock_openai.chat.completions.create = AsyncMock()
        mocker.patch("app.agent.llm_client.AsyncOpenAI", return_value=mock_openai)

        client = LLMClient()
        with pytest.raises(LLMDailyLimitError):
            await client.generate("system", "user")

        mock_openai.chat.completions.create.assert_not_called()

    async def test_handles_empty_content_gracefully(self, mocker):
        mock_openai = MagicMock()
        mock_openai.chat.completions.create = AsyncMock(
            return_value=MagicMock(
                choices=[MagicMock(message=MagicMock(content=None))],
                usage=MagicMock(prompt_tokens=5, completion_tokens=0),
            )
        )
        mocker.patch("app.agent.llm_client.AsyncOpenAI", return_value=mock_openai)

        client = LLMClient()
        result = await client.generate("system", "user")
        assert result == {}
