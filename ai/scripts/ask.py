"""One-liner smoke test against the running Coach Agent.

Calls the live FastAPI route at http://localhost:8081/coach/chat using a
default demo user_id. Lets you ask iteratively without rebuilding JSON
bodies in PowerShell.

Usage (from the `ai/` directory, with venv active and `python main.py` running):

    python -m scripts.ask "qual meu health score esse mês?"

Override the user via env var if you seeded a different one:

    $env:COACH_TEST_USER_ID = "<other-uuid>"; python -m scripts.ask "..."

Override the endpoint (e.g. against staging) similarly with COACH_URL.
"""

from __future__ import annotations

import argparse
import os
import sys

import httpx

DEFAULT_USER_ID = "3511d228-0781-4925-94da-92ec8731d452"
DEFAULT_URL = "http://localhost:8081/coach/chat"


def main(question: str) -> int:
    user_id = os.environ.get("COACH_TEST_USER_ID", DEFAULT_USER_ID)
    url = os.environ.get("COACH_URL", DEFAULT_URL)

    try:
        response = httpx.post(
            url,
            json={"userId": user_id, "question": question},
            timeout=120.0,
        )
    except httpx.ConnectError:
        print(
            "Could not reach the Coach Agent. Is the AI service running? "
            "Start it with `python main.py` from the `ai/` directory.",
            file=sys.stderr,
        )
        return 1

    if response.status_code == 503:
        print(
            "Coach Agent is not configured on the server "
            "(AZURE_FOUNDRY_PROJECT_ENDPOINT missing in ai/.env).",
            file=sys.stderr,
        )
        return 1

    if response.status_code >= 400:
        print(f"HTTP {response.status_code}: {response.text}", file=sys.stderr)
        return 1

    print(response.json()["answer"])
    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Ask the local Coach Agent a question.")
    parser.add_argument("question", help="Question to ask the coach (in any language)")
    args = parser.parse_args()
    sys.exit(main(args.question))
