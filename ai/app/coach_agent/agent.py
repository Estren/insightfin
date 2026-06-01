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
from datetime import date, datetime
from typing import Any
from uuid import UUID

import structlog
from azure.ai.agents.models import (
    FileSearchToolDefinition,
    FileSearchToolResource,
    ToolOutput,
    ToolResources,
)
from azure.ai.projects import AIProjectClient
from azure.ai.projects.aio import AIProjectClient as AsyncAIProjectClient
from azure.identity import DefaultAzureCredential
from azure.identity.aio import DefaultAzureCredential as AsyncDefaultAzureCredential

from app.config import settings
from app.coach_agent.tools import (
    TOOL_DEFINITIONS,
    compare_months,
    get_budget_status,
    get_goals,
    get_health_score,
    get_transactions,
    present_donut_chart,
    present_line_chart,
    project_goal_completion,
    propose_adjust_budget,
    propose_contribute_goal,
    propose_create_budget,
    propose_create_goal,
    propose_log_transaction,
    simulate_budget_change,
)
from app.core_api.client import CoreApiClient

log = structlog.get_logger(__name__)

COACH_INSTRUCTIONS = """\
You are a financial coach embedded in InsightFin, a personal finance platform.
You help the user understand and improve their finances by reasoning over their
real data (transactions, budgets, goals, and a computed health score), grounded
in a small corpus of financial education material.

Rules:
- Always call the appropriate tool before answering numerical questions. Do not
  guess values.
- **Numerical fidelity is critical.** When stating a number that came from a
  tool, use the EXACT value returned. Never round, paraphrase, or approximate
  (if the tool returned 38, say 38 — never "around 37" or "approximately 40").
  This includes zeros: if a breakdown shows 0, say 0 — do not skip or hide it.
- Cite each metric individually when reporting a breakdown. Do not collapse
  distinct values into a single "average" or "roughly".
- **Grounding:** when the user asks about a financial concept (the 50/30/20
  rule, emergency fund, savings rate, goal-setting frameworks, where to cut),
  use the `file_search` tool to retrieve the relevant section from the corpus
  and ground your advice. Briefly reference the source ("according to the
  50/30/20 rule article"). When citations from `file_search` are available,
  include the strongest one or two — they appear inline in your output and
  the UI may render them as footnotes.
- You can PROPOSE actions that change the user's data via the `propose_*` tools
  (create a budget, adjust a budget, create a goal, contribute to a goal, log a
  transaction). They never execute directly — they show the user a confirmation
  card. After calling a `propose_*` tool, ask the
  user to confirm in ONE short sentence (e.g. "Quer que eu crie esse orçamento?").
  NEVER say the action was done — it only happens after the user confirms on the
  card. Only propose when the user asks for it or clearly agrees to a suggestion.
- You can also PRESENT a chart inside your reply via `present_line_chart`
  (evolution over time) or `present_donut_chart` (distribution by category).
  ALWAYS pass real numbers you just received from another tool — never invented
  values. After presenting, write ONE or two short sentences about what the
  chart shows; do not say "see the chart below" — the visual speaks for itself.
  Use sparingly: only when the chart clearly adds value over plain text.
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
        async_project_client: AsyncAIProjectClient,
        core_api: CoreApiClient,
        model: str,
        agent_id: str | None = None,
        vector_store_id: str | None = None,
    ) -> None:
        self._project_client = project_client
        self._async_project_client = async_project_client
        self._core_api = core_api
        self._model = model
        self._vector_store_id = vector_store_id
        if agent_id:
            self._agent_id = agent_id
            self._sync_agent()
        else:
            self._agent_id = self._create_agent()

    @property
    def agent_id(self) -> str:
        return self._agent_id

    def _all_tools(self) -> list:
        """Function tools + the built-in `file_search` when grounding is enabled.

        The file_search tool must be a typed `FileSearchToolDefinition`; the
        SDK validator does `isinstance(tool, FileSearchToolDefinition)` so a
        plain ``{"type": "file_search"}`` dict fails the check.
        """
        tools: list = list(TOOL_DEFINITIONS)
        if self._vector_store_id:
            tools.append(FileSearchToolDefinition())
        return tools

    def _tool_resources(self) -> ToolResources | None:
        """Return typed ToolResources only when grounding is configured.

        Passing a literal ``None`` short-circuits the SDK validator
        (``if tool_resources is None: return``). Passing an empty
        ``ToolResources()`` does NOT work — internal sentinels make
        ``tool_resources.file_search`` evaluate as non-None and the validator
        then demands a file_search tool definition.
        """
        if not self._vector_store_id:
            return None
        return ToolResources(
            file_search=FileSearchToolResource(
                vector_store_ids=[self._vector_store_id]
            )
        )

    def _create_agent(self) -> str:
        agent = self._project_client.agents.create_agent(
            model=self._model,
            name="insightfin-financial-coach",
            instructions=COACH_INSTRUCTIONS,
            tools=self._all_tools(),
            tool_resources=self._tool_resources(),
        )
        log.info(
            "coach_agent_created",
            agent_id=agent.id,
            model=self._model,
            grounding=bool(self._vector_store_id),
        )
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
            tools=self._all_tools(),
            tool_resources=self._tool_resources(),
        )
        log.info(
            "coach_agent_synced",
            agent_id=self._agent_id,
            tool_count=len(TOOL_DEFINITIONS),
            grounding=bool(self._vector_store_id),
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
                    return self._format_with_citations(first.text)
                return str(first)
        return ""

    async def create_thread(self) -> str:
        """Create an empty Foundry thread and return its id.

        Used by the sidebar's lazy thread creation: core-api calls this when
        the user starts a new conversation, persists the returned id, and only
        then streams the first message into it.
        """
        thread = await self._async_project_client.agents.threads.create()
        log.info("coach_thread_created", thread_id=thread.id)
        return thread.id

    async def list_messages(self, thread_id: str) -> list[dict]:
        """Return a thread's full history in chronological order for hydration.

        Foundry returns messages newest-first; we sort ascending by created_at
        so the UI renders top-to-bottom like a normal transcript. Citation
        placeholders are rewritten to [n] markers the same way the live stream
        does, with a per-message source list.
        """
        agents = self._async_project_client.agents
        collected = []
        async for msg in agents.messages.list(thread_id=thread_id):
            collected.append(msg)
        collected.sort(key=lambda m: getattr(m, "created_at", 0) or 0)

        history: list[dict] = []
        for msg in collected:
            if not msg.content:
                continue
            first = msg.content[0]
            text_obj = getattr(first, "text", None)
            if text_obj is None:
                continue

            text = text_obj.value
            citations: list[dict] = []
            marker_map: dict[str, int] = {}
            for ann in getattr(text_obj, "annotations", None) or []:
                file_citation = getattr(ann, "file_citation", None)
                if file_citation is None:
                    continue
                file_id = getattr(file_citation, "file_id", None)
                placeholder = getattr(ann, "text", None)
                if not file_id:
                    continue
                if file_id not in marker_map:
                    marker_map[file_id] = len(marker_map) + 1
                    filename = await self._lookup_filename(file_id)
                    citations.append({"marker": marker_map[file_id], "filename": filename})
                if placeholder:
                    text = text.replace(placeholder, f" [{marker_map[file_id]}]")

            history.append(
                {
                    "role": msg.role,
                    "text": text,
                    "citations": citations,
                    "createdAt": self._iso_local(getattr(msg, "created_at", None)),
                }
            )
        return history

    @staticmethod
    def _iso_local(created) -> str | None:
        """Normalize Foundry's created_at (epoch int or datetime) to naive ISO.

        Core-api parses this with LocalDateTime.parse, which rejects timezone
        offsets — so we emit a naive local-time ISO string (or None).
        """
        if created is None:
            return None
        if isinstance(created, (int, float)):
            return datetime.fromtimestamp(created).isoformat()
        if hasattr(created, "isoformat"):
            return created.replace(tzinfo=None).isoformat() if getattr(created, "tzinfo", None) else created.isoformat()
        return None

    async def ask_stream(
        self, user_id: UUID, question: str, thread_id: str | None = None
    ):
        """Stream the agent's reasoning as a sequence of structured events.

        Yields dicts with a ``type`` field — the FastAPI route maps these to
        SSE event names. Event types:

        - ``thread`` → ``{"id": "thread_..."}``: emitted once per stream so the
          client can keep the same thread across follow-up requests (multi-turn).
        - ``token``   → ``{"data": "<text chunk>"}``: append to assistant bubble.
        - ``tool_call`` → ``{"name": "get_health_score"}``: show "thinking" tag.
        - ``tool_executed`` → ``{"name", "args", "result"}``: append to the
          assistant message's reasoning trail (expandable in the UI).
        - ``action_proposal`` → ``{"action", "params", "summary"}``: render a
          confirmation card; the user approves before core-api executes.
        - ``chart_payload`` → ``{"kind", "title", "data"}``: render a chart in
          the current assistant bubble. Lives alongside the explanatory text.
        - ``citation`` → ``{"marker": 1, "filename": "regra-50-30-20.md"}``.
        - ``error``   → ``{"data": "<message>"}``: render and stop.
        - ``done``    → end of stream.

        When ``thread_id`` is provided the existing Foundry thread is reused —
        previous user/assistant turns stay in context, so follow-up questions
        like "e por quê?" work. When omitted, a brand new thread is created
        and its id is sent in the very first event for the client to capture.

        Tool dispatch is done in this method so ``user_id`` stays bound via
        closure and never enters the LLM-visible tool schema.
        """
        agents = self._async_project_client.agents

        if thread_id:
            thread = await agents.threads.get(thread_id)
        else:
            thread = await agents.threads.create()
        yield {"type": "thread", "id": thread.id}

        await agents.messages.create(
            thread_id=thread.id, role="user", content=question
        )

        today = date.today()
        additional_instructions = (
            f"Today's date is {today.isoformat()}. "
            f"The current month (YYYY-MM) is {today.strftime('%Y-%m')}. "
            f"Use this when the user says 'this month', 'last month', etc."
        )

        file_to_marker: dict[str, int] = {}

        async with await agents.runs.stream(
            thread_id=thread.id,
            agent_id=self._agent_id,
            additional_instructions=additional_instructions,
        ) as handler:
            async for event_type, event_data, _ in handler:
                event_name = str(event_type)

                if event_name.endswith("message.delta"):
                    delta = getattr(event_data, "delta", None)
                    content = getattr(delta, "content", []) if delta else []
                    for block in content:
                        text = getattr(block, "text", None)
                        if text is not None and getattr(text, "value", None):
                            yield {"type": "token", "data": text.value}

                elif event_name.endswith("requires_action") or (
                    hasattr(event_data, "status")
                    and getattr(event_data, "status", None) == "requires_action"
                ):
                    required_action = getattr(event_data, "required_action", None)
                    if required_action is None:
                        continue
                    tool_calls = required_action.submit_tool_outputs.tool_calls
                    outputs: list[ToolOutput] = []
                    for call in tool_calls:
                        name = call.function.name
                        try:
                            args = json.loads(call.function.arguments or "{}")
                        except json.JSONDecodeError:
                            args = {}
                        yield {
                            "type": "tool_call",
                            "name": name,
                        }
                        result = await self._dispatch_tool(call, user_id)
                        # Surface the executed call to the frontend so it can
                        # render an expandable "reasoning trail" under the
                        # assistant message — name, args from Foundry, and the
                        # full result dict the agent received back. Lets the
                        # user see exactly which tools fired with which
                        # inputs and outputs.
                        yield {
                            "type": "tool_executed",
                            "name": name,
                            "args": args,
                            "result": result,
                        }
                        # A successful proposal (no "error") is surfaced to the
                        # client as a confirmation card; the user must approve it
                        # before core-api executes anything.
                        if result.get("action") and "error" not in result:
                            yield {
                                "type": "action_proposal",
                                "action": result["action"],
                                "params": result.get("params", {}),
                                "summary": result.get("summary", ""),
                            }
                        # Charts the agent decided to show are surfaced as a
                        # separate SSE event; the frontend renders them inside
                        # the same assistant message bubble.
                        if result.get("status") == "presented" and "error" not in result:
                            yield {
                                "type": "chart_payload",
                                "kind": result.get("kind"),
                                "title": result.get("title", ""),
                                "data": result.get("data", {}),
                            }
                        outputs.append(
                            ToolOutput(
                                tool_call_id=call.id,
                                output=json.dumps(result),
                            )
                        )
                    await agents.runs.submit_tool_outputs_stream(
                        thread_id=thread.id,
                        run_id=event_data.id,
                        tool_outputs=outputs,
                        event_handler=handler,
                    )

                elif event_name.endswith("message.completed"):
                    content = getattr(event_data, "content", []) or []
                    for block in content:
                        text_obj = getattr(block, "text", None)
                        if text_obj is None:
                            continue
                        annotations = getattr(text_obj, "annotations", None) or []
                        for ann in annotations:
                            file_citation = getattr(ann, "file_citation", None)
                            if file_citation is None:
                                continue
                            file_id = getattr(file_citation, "file_id", None)
                            if not file_id:
                                continue
                            if file_id not in file_to_marker:
                                file_to_marker[file_id] = len(file_to_marker) + 1
                                filename = await self._lookup_filename(file_id)
                                yield {
                                    "type": "citation",
                                    "marker": file_to_marker[file_id],
                                    "filename": filename,
                                }

                elif event_name.endswith("run.failed") or event_name.endswith(
                    "run.cancelled"
                ):
                    last_error = getattr(event_data, "last_error", None)
                    log.error(
                        "coach_stream_failed",
                        event=event_name,
                        last_error=str(last_error) if last_error else None,
                    )
                    yield {
                        "type": "error",
                        "data": str(last_error) if last_error else event_name,
                    }
                    return

                elif event_name == "error":
                    log.error("coach_stream_error", payload=str(event_data))
                    yield {"type": "error", "data": str(event_data)}
                    return

                elif event_name.endswith("run.completed"):
                    # The run finished cleanly. `done` events that follow are
                    # HTTP-chunk markers — they appear after each segment of a
                    # multi-segment stream (e.g. one segment per submit_tool_-
                    # outputs round trip), not at logical end of the reasoning.
                    break

                # `done` events are intentionally ignored here: they signal the
                # end of an HTTP segment, not of the agent's reasoning. The
                # next segment chains in via submit_tool_outputs_stream and
                # async_chain on the handler's response iterator.

        yield {"type": "done"}

    async def _lookup_filename(self, file_id: str) -> str:
        """Resolve a Foundry file id to its original filename for citations."""
        try:
            info = await self._async_project_client.agents.files.get(file_id)
            return getattr(info, "filename", file_id)
        except Exception as exc:
            log.warning("coach_file_lookup_failed", file_id=file_id, error=str(exc))
            return file_id

    def _format_with_citations(self, text_payload: Any) -> str:
        """Replace inline SDK citation placeholders with [n] markers + footer.

        Raw assistant text has annotations like `【4:0†source】` embedded inline.
        We map each unique file id to a sequential [n] marker and append a
        ``Fontes:`` list at the end so a CLI/HTTP consumer sees readable
        citations without needing to parse annotation objects.
        """
        raw = text_payload.value
        annotations = getattr(text_payload, "annotations", None) or []
        if not annotations:
            return raw

        agents = self._project_client.agents
        file_to_marker: dict[str, int] = {}
        markers: list[tuple[int, str]] = []

        for ann in annotations:
            file_citation = getattr(ann, "file_citation", None)
            if file_citation is None:
                continue
            file_id = getattr(file_citation, "file_id", None)
            placeholder = getattr(ann, "text", None)
            if not file_id or not placeholder:
                continue
            if file_id not in file_to_marker:
                file_to_marker[file_id] = len(file_to_marker) + 1
                markers.append((file_to_marker[file_id], file_id))
            raw = raw.replace(placeholder, f" [{file_to_marker[file_id]}]")

        if not markers:
            return raw

        footer_lines = ["", "Fontes:"]
        for idx, file_id in markers:
            try:
                file_info = agents.files.get(file_id)
                name = getattr(file_info, "filename", file_id)
            except Exception:
                name = file_id
            footer_lines.append(f"  [{idx}] {name}")

        return raw + "\n" + "\n".join(footer_lines)

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
            if name == "propose_create_budget":
                # Proposal only — never mutates. The streaming loop turns this
                # into an `action_proposal` event; execution happens in core-api
                # after the user confirms.
                return propose_create_budget(args["category"], float(args["amount"]))
            if name == "propose_contribute_goal":
                return propose_contribute_goal(args["goal_title"], float(args["amount"]))
            if name == "propose_create_goal":
                return propose_create_goal(
                    args["title"], float(args["target_amount"]), args.get("deadline")
                )
            if name == "propose_adjust_budget":
                return propose_adjust_budget(args["category"], float(args["amount"]))
            if name == "propose_log_transaction":
                return propose_log_transaction(
                    args["type"], args["category"], float(args["amount"]), args.get("description")
                )
            if name == "present_line_chart":
                # Presentation only — never mutates. The streaming loop turns the
                # descriptor into a `chart_payload` event the frontend renders
                # inside the assistant bubble.
                return present_line_chart(
                    args["title"], args["categories"], args["series"]
                )
            if name == "present_donut_chart":
                return present_donut_chart(
                    args["title"], args["labels"], args["values"]
                )
            return {"error": f"unknown tool {name}"}
        except Exception as exc:  # surface tool errors back to the agent
            log.error("coach_tool_failed", name=name, error=str(exc))
            return {"error": str(exc)}


def build_project_client() -> AIProjectClient:
    """Build the sync Foundry project client (used for setup/update of the agent).

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


def build_async_project_client() -> AsyncAIProjectClient:
    """Build the async Foundry project client (used for streaming runs).

    Streaming uses async iteration over server-sent events from Foundry;
    a separate async client + async credential keeps the asyncio loop clean.
    """
    if not settings.azure_foundry_project_endpoint:
        raise RuntimeError("AZURE_FOUNDRY_PROJECT_ENDPOINT is not set")

    return AsyncAIProjectClient(
        endpoint=settings.azure_foundry_project_endpoint,
        credential=AsyncDefaultAzureCredential(),
    )
