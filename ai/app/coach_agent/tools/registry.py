"""Tool schemas exposed to the Foundry agent.

The `user_id` is intentionally absent from every schema — it is bound to the
agent state by `FoundryCoachAgent` and never exposed to the LLM. Passing it as
a tool argument would let a prompt-injected message read another user's data.
"""

TOOL_DEFINITIONS: list[dict] = [
    {
        "type": "function",
        "function": {
            "name": "get_health_score",
            "description": (
                "Return the user's financial health score for a given month, "
                "broken down into savings rate, budget adherence, goal progress "
                "and expense consistency (each 0-100). Use this when the user "
                "asks about their score, how they are doing financially, or why "
                "their score changed."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "month": {
                        "type": "string",
                        "pattern": "^[0-9]{4}-(0[1-9]|1[0-2])$",
                        "description": "Target month in YYYY-MM format (e.g. '2026-05').",
                    },
                },
                "required": ["month"],
            },
        },
    },
]
