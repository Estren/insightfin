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
                "their score changed. Call it twice (current + previous month) "
                "when you need to explain a change."
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
    {
        "type": "function",
        "function": {
            "name": "get_transactions",
            "description": (
                "Return an aggregated summary of the user's transactions for a "
                "given month: total income, total expenses, balance, transaction "
                "count, and a per-category breakdown sorted by total amount "
                "(largest first). Use this to compare months, identify which "
                "categories drove a change, or surface where the user is spending "
                "the most. Do NOT use it for the health score — that has a "
                "dedicated tool."
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
    {
        "type": "function",
        "function": {
            "name": "get_budget_status",
            "description": (
                "Return the user's budgets for a given month with usage tracking: "
                "budget amount, amount spent, percentage used, remaining, and "
                "status flag ('within', 'near_limit' for >=80%, or 'over' for "
                ">100%). Use this when the user asks about budgets, whether they "
                "can afford to spend X on Y, how close they are to limits, or "
                "which categories blew past the budget. Returns counts (overCount, "
                "nearLimitCount) for quick summary answers."
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
