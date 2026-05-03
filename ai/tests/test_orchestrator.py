from __future__ import annotations

from unittest.mock import AsyncMock, MagicMock
from uuid import uuid4

import pytest

from app.agent.llm_client import LLMDailyLimitError
from app.agent.orchestrator import Orchestrator


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def make_orchestrator(context=None, llm_result=None):
    core_api = AsyncMock()
    core_api.get_user_context.return_value = context or {
        "transactions": [MagicMock()],
        "budgets": [],
        "goals": [],
        "categories": [],
    }
    core_api.post_feedback = AsyncMock()

    llm = AsyncMock()
    llm.generate.return_value = llm_result or {"title": "Test", "content": "Some content."}

    return Orchestrator(core_api=core_api, llm=llm), core_api, llm


class Mock409Error(Exception):
    def __init__(self):
        self.response = MagicMock()
        self.response.status_code = 409


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestAnalyze:
    async def test_raises_on_unknown_analysis_type(self):
        orchestrator, _, _ = make_orchestrator()
        with pytest.raises(ValueError, match="Unknown analysis type"):
            await orchestrator.analyze(uuid4(), "INVALID_TYPE")

    async def test_skips_when_all_lists_are_empty(self):
        orchestrator, core_api, llm = make_orchestrator(
            context={"transactions": [], "budgets": [], "goals": [], "categories": []}
        )
        await orchestrator.analyze(uuid4(), "ALERT")
        llm.generate.assert_not_called()
        core_api.post_feedback.assert_not_called()

    async def test_skips_when_context_builder_returns_none(self, mocker):
        # ALERT with no budget over 80% → context_builder returns None
        orchestrator, core_api, llm = make_orchestrator(
            context={
                "transactions": [],
                "budgets": [MagicMock(percentageUsed=50.0)],  # under threshold
                "goals": [],
                "categories": [],
            }
        )
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value=None)
        await orchestrator.analyze(uuid4(), "ALERT")
        llm.generate.assert_not_called()
        core_api.post_feedback.assert_not_called()

    async def test_skips_on_daily_limit_error(self, mocker):
        orchestrator, core_api, llm = make_orchestrator()
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {},
            "prompt_context": "ctx",
        })
        llm.generate.side_effect = LLMDailyLimitError("limit reached")

        await orchestrator.analyze(uuid4(), "MONTHLY_REPORT")  # must not raise
        core_api.post_feedback.assert_not_called()

    async def test_skips_on_409_idempotency(self, mocker):
        orchestrator, core_api, llm = make_orchestrator()
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {},
            "prompt_context": "ctx",
        })
        core_api.post_feedback.side_effect = Mock409Error()

        await orchestrator.analyze(uuid4(), "HEALTH_SCORE")  # must not raise

    async def test_raises_on_llm_error(self, mocker):
        orchestrator, _, llm = make_orchestrator()
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {},
            "prompt_context": "ctx",
        })
        llm.generate.side_effect = RuntimeError("API failure")

        with pytest.raises(RuntimeError):
            await orchestrator.analyze(uuid4(), "MONTHLY_REPORT")

    async def test_happy_path_calls_post_feedback(self, mocker):
        user_id = uuid4()
        orchestrator, core_api, llm = make_orchestrator(
            llm_result={"title": "Your Report", "content": "Great job!"}
        )
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {"score": 72},
            "prompt_context": "ctx",
        })

        await orchestrator.analyze(user_id, "HEALTH_SCORE", month="2026-05")

        core_api.post_feedback.assert_called_once()
        payload = core_api.post_feedback.call_args[0][0]
        assert payload["userId"] == str(user_id)
        assert payload["type"] == "HEALTH_SCORE"
        assert payload["title"] == "Your Report"
        assert payload["content"] == "Great job!"
        assert payload["referenceMonth"] == "2026-05"

    async def test_happy_path_serializes_metadata_as_json_string(self, mocker):
        orchestrator, core_api, _ = make_orchestrator()
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {"score": 80, "breakdown": {"savingsRate": 90}},
            "prompt_context": "ctx",
        })

        await orchestrator.analyze(uuid4(), "HEALTH_SCORE")

        payload = core_api.post_feedback.call_args[0][0]
        assert isinstance(payload["metadata"], str)

        import json
        parsed = json.loads(payload["metadata"])
        assert parsed["score"] == 80

    async def test_monthly_report_includes_highlights_in_metadata(self, mocker):
        orchestrator, core_api, llm = make_orchestrator(
            llm_result={"title": "Report", "content": "Good.", "highlights": ["Saved more!"]}
        )
        mocker.patch("app.agent.orchestrator.context_builder.build", return_value={
            "computed_metadata": {"totalIncome": 5000.0},
            "prompt_context": "ctx",
        })

        await orchestrator.analyze(uuid4(), "MONTHLY_REPORT")

        payload = core_api.post_feedback.call_args[0][0]
        import json
        parsed = json.loads(payload["metadata"])
        assert parsed["highlights"] == ["Saved more!"]
