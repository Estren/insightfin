"""Foundry-backed financial coach.

Wraps the synchronous `azure-ai-projects` SDK behind an async-friendly
interface. Per ai/CLAUDE.md, `user_id` is bound to the agent state via the
`ask()` argument and **never** exposed in the tool schemas the LLM sees.

The agent is provisioned once (on first `ask()` if AZURE_FOUNDRY_AGENT_ID is
empty) and then reused. Set AZURE_FOUNDRY_AGENT_ID in `.env` to skip the
create step on subsequent runs.
"""

from __future__ import annotations

import asyncio
import json
from datetime import date
from typing import Any
from uuid import UUID

import structlog
from azure.ai.projects import AIProjectClient
from azure.identity import DefaultAzureCredential

from app.config import settings
from app.coach_agent.tools import (
    TOOL_DEFINITIONS,
    compare_months,
    get_budget_status,
    get_goals,
    get_health_score,
    get_transactions,
    project_goal_completion,
    simulate_budget_change,
)
from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)

COACH_INSTRUCTIONS = """\
You are a financial coach embedded in InsightFin, a personal finance platform.
You help the user understand and improve their finances by reasoning over their
real data (transactions, budgets, goals, and a computed health score).

Rules:
- Always call the appropriate tool before answering numerical questions. Do not
  guess values.
- **Numerical fidelity is critical.** When stating a number that came from a
  tool, use the EXACT value returned. Never round, paraphrase, or approximate
  (if the tool returned 38, say 38 — never "around 37" or "approximately 40").
  This includes zeros: if a breakdown shows 0, say 0 — do not skip or hide it.
- Cite each metric individually when reporting a breakdown. Do not collapse
  distinct values into a single "average" or "roughly".
- The user's id is bound to your session — never ask for it, never accept it as
  an argument.
- When the user says "this month" without specifying, use the current month in
  YYYY-MM format (from the additional instructions for today's date). Likewise
  "last month" = previous month.
- Respond in the same language as the user's question (Portuguese or English).
- Keep responses concise (3-5 sentences) unless the user explicitly asks for
  detail. Lead with the headline number, then explain.
"""


class FoundryCoachAgent:
    def __init__(
        self,
        project_client: AIProjectClient,
        core_api: CoreApiClient,
        model: str,
        agent_id: str | None = None,
    ) -> None:
        self._project_client = project_client
        self._core_api = core_api
        self._model = model
        if agent_id:
            self._agent_id = agent_id
            self._sync_agent()
        else:
            self._agent_id = self._create_agent()

    @property
    def agent_id(self) -> str:
        return self._agent_id

    def _create_agent(self) -> str:
        agent = self._project_client.agents.create_agent(
            model=self._model,
            name="insightfin-financial-coach",
            instructions=COACH_INSTRUCTIONS,
            tools=TOOL_DEFINITIONS,
        )
        log.info("coach_agent_created", agent_id=agent.id, model=self._model)
        return agent.id

    def _sync_agent(self) -> None:
        """Keep the deployed agent in sync with the code's tools and instructions.

        Without this, adding or modifying a tool requires manually clearing
        ``AZURE_FOUNDRY_AGENT_ID`` so a new agent gets provisioned. Running
        ``update_agent`` on every startup is cheap and removes that footgun.
        """
        self._project_client.agents.update_agent(
            agent_id=self._agent_id,
            model=self._model,
            instructions=COACH_INSTRUCTIONS,
            tools=TOOL_DEFINITIONS,
        )
        log.info(
            "coach_agent_synced",
            agent_id=self._agent_id,
            tool_count=len(TOOL_DEFINITIONS),
        )

    async def ask(self, user_id: UUID, question: str) -> str:
        """Send `question` to the agent and return its final reply.

        Polls the run, executing tool calls inline with `user_id` bound from
        this method's argument. Raises on agent failure.
        """
        agents = self._project_client.agents

        thread = await asyncio.to_thread(agents.threads.create)
        await asyncio.to_thread(
            agents.messages.create,
            thread_id=thread.id,
            role="user",
            content=question,
        )
        today = date.today()
        run = await asyncio.to_thread(
            agents.runs.create,
            thread_id=thread.id,
            agent_id=self._agent_id,
            additional_instructions=(
                f"Today's date is {today.isoformat()}. "
                f"The current month (YYYY-MM) is {today.strftime('%Y-%m')}. "
                f"Use this when the user says 'this month', 'last month', etc."
            ),
        )

        while True:
            run = await asyncio.to_thread(
                agents.runs.get, thread_id=thread.id, run_id=run.id
            )
            status = run.status

            if status == "requires_action":
                tool_calls = run.required_action.submit_tool_outputs.tool_calls
                outputs = []
                for call in tool_calls:
                    result = await self._dispatch_tool(call, user_id)
                    outputs.append(
                        {"tool_call_id": call.id, "output": json.dumps(result)}
                    )
                await asyncio.to_thread(
                    agents.runs.submit_tool_outputs,
                    thread_id=thread.id,
                    run_id=run.id,
                    tool_outputs=outputs,
                )
            elif status == "completed":
                break
            elif status in ("failed", "cancelled", "expired"):
                log.error(
                    "coach_run_failed",
                    status=status,
                    last_error=getattr(run, "last_error", None),
                )
                raise RuntimeError(f"Coach run {status}: {getattr(run, 'last_error', '')}")
            else:
                await asyncio.sleep(0.5)

        messages = await asyncio.to_thread(
            agents.messages.list, thread_id=thread.id
        )
        for msg in messages:
            if msg.role == "assistant" and msg.content:
                first = msg.content[0]
                if hasattr(first, "text") and hasattr(first.text, "value"):
                    return first.text.value
                return str(first)
        return ""

    async def _dispatch_tool(self, tool_call: Any, user_id: UUID) -> dict:
        name = tool_call.function.name
        try:
            args = json.loads(tool_call.function.arguments)
        except json.JSONDecodeError as exc:
            log.error("coach_tool_bad_args", name=name, error=str(exc))
            return {"error": "invalid arguments"}

        log.info("coach_tool_dispatch", name=name, args=args, user_id=str(user_id))

        try:
            if name == "get_health_score":
                return await get_health_score(self._core_api, user_id, args["month"])
            if name == "get_transactions":
                return await get_transactions(self._core_api, user_id, args["month"])
            if name == "get_budget_status":
                return await get_budget_status(self._core_api, user_id, args["month"])
            if name == "get_goals":
                return await get_goals(self._core_api, user_id)
            if name == "compare_months":
                return await compare_months(
                    self._core_api,
                    user_id,
                    args["current_month"],
                    args["previous_month"],
                )
            if name == "project_goal_completion":
                return await project_goal_completion(
                    self._core_api,
                    user_id,
                    args["goal_title"],
                    float(args["monthly_contribution"]),
                )
            if name == "simulate_budget_change":
                return await simulate_budget_change(
                    self._core_api,
                    user_id,
                    args["category"],
                    float(args["additional_amount"]),
                    args["month"],
                )
            return {"error": f"unknown tool {name}"}
        except Exception as exc:  # surface tool errors back to the agent
            log.error("coach_tool_failed", name=name, error=str(exc))
            return {"error": str(exc)}


def build_project_client() -> AIProjectClient:
    """Build the Foundry project client from settings.

    Authenticates via `DefaultAzureCredential` — the SDK 1.0 does not accept
    API key credentials. Locally, this picks up `az login`. In Azure Container
    Apps, it will pick up the managed identity.
    """
    if not settings.azure_foundry_project_endpoint:
        raise RuntimeError("AZURE_FOUNDRY_PROJECT_ENDPOINT is not set")

    return AIProjectClient(
        endpoint=settings.azure_foundry_project_endpoint,
        credential=DefaultAzureCredential(),
    )
