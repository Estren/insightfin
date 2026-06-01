"""Defense-in-depth auth on the AI service's HTTP surface.

The AI service has internal-only ACA ingress, so the public internet can't
reach it. But every other container in the same `insightfin-env` could
otherwise call its endpoints without restriction. This middleware requires
the shared `X-Internal-Auth` header (same value core-api expects on its
own `/internal/*` routes) on every request that isn't a health probe or
Prometheus scrape — turning the network isolation into proper authentication.

Fail-secure by design: if `INTERNAL_SHARED_SECRET` is empty/unset, **every**
non-bypass request returns 401 instead of silently accepting traffic.
"""

from __future__ import annotations

from typing import Awaitable, Callable

import structlog
from fastapi import Request, Response
from fastapi.responses import JSONResponse

from app.config import settings

log = structlog.get_logger(__name__)

_HEADER = "x-internal-auth"

# Paths that must remain reachable without the secret. Health is used by the
# ACA liveness/readiness probe; metrics by the Prometheus scrape; docs and
# openapi by FastAPI's built-in docs UI. All four are read-only and expose
# no user data.
_BYPASS_PREFIXES: tuple[str, ...] = (
    "/health",
    "/metrics",
    "/docs",
    "/redoc",
    "/openapi.json",
)


def _is_bypass(path: str) -> bool:
    return any(path == prefix or path.startswith(prefix + "/") for prefix in _BYPASS_PREFIXES) or path == "/"


async def internal_auth_middleware(
    request: Request,
    call_next: Callable[[Request], Awaitable[Response]],
) -> Response:
    path = request.url.path
    if _is_bypass(path):
        return await call_next(request)

    expected = settings.internal_shared_secret
    if not expected:
        log.warning("internal_auth_unconfigured", path=path)
        return JSONResponse(status_code=401, content={"detail": "Unauthorized"})

    presented = request.headers.get(_HEADER)
    if presented != expected:
        log.warning("internal_auth_rejected", path=path, has_header=presented is not None)
        return JSONResponse(status_code=401, content={"detail": "Unauthorized"})

    return await call_next(request)
