"""Provision the Foundry IQ vector store for the Coach Agent.

One-shot setup: uploads every `.md` file in `ai/coach_corpus/` to the
Foundry project, creates a vector store from those files, and prints
the vector store id. Paste that id into `ai/.env` as
`AZURE_FOUNDRY_VECTOR_STORE_ID` and the Coach Agent will start grounding
its answers in the corpus.

Usage (from the `ai/` directory, with venv active and `.env` populated):

    python -m scripts.setup_foundry_iq

Safe to re-run: it does NOT delete existing vector stores. Each run
creates a new one — set the new id in `.env` and delete the old one
manually via the portal if you want to clean up.
"""

from __future__ import annotations

import sys
from pathlib import Path

# Force UTF-8 on stdout/stderr so the checkmark glyph renders on Windows
# consoles (default cp1252 can't encode it and raises mid-run).
sys.stdout.reconfigure(encoding="utf-8")
sys.stderr.reconfigure(encoding="utf-8")

# Make `app` importable when running from `ai/`.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.coach_agent.agent import build_project_client  # noqa: E402

CORPUS_DIR = Path(__file__).resolve().parents[1] / "coach_corpus"
VECTOR_STORE_NAME = "insightfin-financial-education"


def main() -> int:
    if not CORPUS_DIR.is_dir():
        print(f"Corpus directory not found: {CORPUS_DIR}", file=sys.stderr)
        return 1

    md_files = sorted(CORPUS_DIR.glob("*.md"))
    if not md_files:
        print(f"No markdown files in {CORPUS_DIR}", file=sys.stderr)
        return 1

    print(f"Found {len(md_files)} corpus files:")
    for f in md_files:
        print(f"  - {f.name}")
    print()

    project_client = build_project_client()
    agents = project_client.agents

    print("Uploading files to Foundry...")
    file_ids: list[str] = []
    for f in md_files:
        uploaded = agents.files.upload_and_poll(file_path=str(f), purpose="assistants")
        file_ids.append(uploaded.id)
        print(f"  ✓ {f.name} → {uploaded.id}")

    print()
    print("Creating vector store...")
    vector_store = agents.vector_stores.create_and_poll(
        file_ids=file_ids,
        name=VECTOR_STORE_NAME,
    )
    print(f"  ✓ {vector_store.name} → {vector_store.id}")

    print()
    print("=" * 60)
    print(f"AZURE_FOUNDRY_VECTOR_STORE_ID={vector_store.id}")
    print("=" * 60)
    print()
    print("Paste the line above into ai/.env, then restart `python main.py`.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
