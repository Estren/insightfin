"""Per-user rate limit on the LLM-consuming Coach endpoints.

The AI service has a global `MAX_LLM_CALLS_PER_DAY` cap shared across every
user. Without a per-user limit, a single authenticated client could exhaust
the entire daily quota (and run up the Foundry bill in the process), denying
service to everyone else.

This module keeps a rolling per-user window in memory. The window length and
allowed call count are configurable; the default is 30 calls per hour per
user — generous for legitimate exploration, but enough to blunt scripted
abuse before the global cap fires.

Tradeoffs:
- In-memory, per replica. Acceptable for hackathon scale; for distributed
  deploys we'd back it with Redis or upstash.
- Uses `time.monotonic()` so it's immune to wall-clock skew but resets on
  process restart — also acceptable here (deploys are not that frequent).
"""

from __future__ import annotations

from collections import deque
from threading import Lock
from time import monotonic
from typing import Deque
from uuid import UUID

from app.config import settings


_lock = Lock()
_per_user: dict[str, Deque[float]] = {}


def check_quota(user_id: UUID) -> tuple[bool, int]:
    """Returns (allowed, retry_after_seconds).

    `allowed=True` means the call is recorded and may proceed. `allowed=False`
    means the caller has already used its budget within the window — no
    record is added and `retry_after_seconds` is the time until the oldest
    in-window call ages out and frees a slot.
    """
    window_seconds = settings.coach_rate_limit_window_seconds
    max_calls = settings.coach_rate_limit_max_calls
    if max_calls <= 0:
        return True, 0

    now = monotonic()
    key = str(user_id)
    with _lock:
        history = _per_user.setdefault(key, deque())
        # Drop entries that fell out of the window.
        while history and now - history[0] > window_seconds:
            history.popleft()
        if len(history) >= max_calls:
            retry = max(1, int(window_seconds - (now - history[0])))
            return False, retry
        history.append(now)
        return True, 0


def reset_for_tests() -> None:
    """Test helper — wipe the per-user state between tests."""
    with _lock:
        _per_user.clear()
