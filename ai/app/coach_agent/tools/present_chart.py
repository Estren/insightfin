"""Chart presentation tools for the Coach.

Like the ``propose_*`` tools these functions never mutate anything; they
validate the LLM's chart request and return a structured descriptor that the
streaming layer surfaces as a ``chart_payload`` SSE event. The frontend renders
the chart inside the same assistant message bubble.

We expose **two distinct tools** (``present_line_chart`` and
``present_donut_chart``) instead of one parametric tool because GPT-4.1 mini
struggles to obey a free-form ``data`` object even with a detailed description
— it invents its own shape. Two narrow schemas let the model fill primitive
arrays it can't misroute.

**Persistence is intentionally out of v1.** Foundry threads only retain text +
annotations on rehydration, so a chart shown live disappears when the thread
is reloaded. The agent's textual explanation alongside the chart stays — the
answer still reads correctly. Persistence would require a parallel store and
is deferred.
"""

from __future__ import annotations

from typing import Any


class ChartError(ValueError):
    """Raised when a chart request is malformed."""


def present_line_chart(
    title: str,
    categories: list[str],
    series: list[dict[str, Any]],
) -> dict[str, Any]:
    """Validate a line-chart request and return the descriptor for the SSE layer."""
    clean_title = _clean_title(title)
    if not isinstance(categories, list) or not categories:
        raise ChartError("categories must be a non-empty list")
    if not all(isinstance(c, str) and c.strip() for c in categories):
        raise ChartError("each category must be a non-empty string")
    if not isinstance(series, list) or not series:
        raise ChartError("series must be a non-empty list")
    for entry in series:
        if not isinstance(entry, dict):
            raise ChartError("each series entry must be an object")
        name = entry.get("name")
        points = entry.get("data")
        if not isinstance(name, str) or not name.strip():
            raise ChartError("each series entry must have a non-empty name")
        if not isinstance(points, list) or len(points) != len(categories):
            raise ChartError(
                "each series.data must be a list of the same length as categories"
            )
        for value in points:
            if not isinstance(value, (int, float)):
                raise ChartError("series.data values must be numbers")
    return {
        "status": "presented",
        "kind": "line",
        "title": clean_title,
        "data": {"categories": list(categories), "series": list(series)},
    }


def present_donut_chart(
    title: str,
    labels: list[str],
    values: list[float],
) -> dict[str, Any]:
    """Validate a donut-chart request and return the descriptor for the SSE layer."""
    clean_title = _clean_title(title)
    if not isinstance(labels, list) or not labels:
        raise ChartError("labels must be a non-empty list")
    if not all(isinstance(l, str) and l.strip() for l in labels):
        raise ChartError("each label must be a non-empty string")
    if not isinstance(values, list) or not values:
        raise ChartError("values must be a non-empty list")
    if len(labels) != len(values):
        raise ChartError("labels and values must have the same length")
    for value in values:
        if not isinstance(value, (int, float)):
            raise ChartError("values must be numbers")
    return {
        "status": "presented",
        "kind": "donut",
        "title": clean_title,
        "data": {"labels": list(labels), "series": list(values)},
    }


def _clean_title(title: str) -> str:
    clean = (title or "").strip()
    if not clean:
        raise ChartError("title is required")
    return clean
