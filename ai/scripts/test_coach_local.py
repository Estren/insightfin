"""Smoke test for the Foundry Coach Agent against the local core-api.

Usage (from the `ai/` directory, with venv active and `.env` populated):

    python -m scripts.test_coach_local <user_id> "your question"

Example:

    python -m scripts.test_coach_local 7ab022f9-a5c8-45ff-9ab8-7562dead8ef2 \
        "qual meu health score em 2026-05?"

This script bypasses FastAPI and Kafka — it builds the same components the
service `lifespan` builds, then calls the agent directly. It does NOT touch
the existing batch orchestrator.
"""

from __future__ import annotations

import argparse
import asyncio
import os
import sys
from pathlib import Path
from uuid import UUID

# Make `app` importable when running from `ai/` as `python -m scripts.test_coach_local`
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

# Default to localhost for local runs — `.env` has the docker hostname.
os.environ.setdefault("CORE_API_URL", "http://localhost:8080")

from app.coach_agent.agent import FoundryCoachAgent, build_project_client  # noqa: E402
from app.config import settings  # noqa: E402
from app.core_api.client import CoreApiClient, make_http_client  # noqa: E402


async def main(user_id: UUID, question: str) -> int:
    http = make_http_client()
    core_api = CoreApiClient(http)
    project_client = build_project_client()

    agent = FoundryCoachAgent(
        project_client=project_client,
        core_api=core_api,
        model=settings.azure_foundry_model,
        agent_id=settings.azure_foundry_agent_id or None,
    )

    print(f"\n--- agent_id: {agent.agent_id}")
    print(f"--- user_id: {user_id}")
    print(f"--- question: {question}\n")

    try:
        answer = await agent.ask(user_id, question)
    finally:
        await http.aclose()

    print("--- answer ---")
    print(answer)
    print()
    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("user_id", type=UUID, help="UUID of an existing user")
    parser.add_argument("question", type=str, help="The question to ask the agent")
    args = parser.parse_args()
    sys.exit(asyncio.run(main(args.user_id, args.question)))
