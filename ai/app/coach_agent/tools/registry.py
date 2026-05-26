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
                ">100%). Use this when the user asks about budgets, how close "
                "they are to limits, or which categories blew past the budget. "
                "Returns counts (overCount, nearLimitCount) for quick summary "
                "answers. For 'can I spend X more in Y?' questions, prefer "
                "simulate_budget_change instead."
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
            "name": "get_goals",
            "description": (
                "List the user's financial goals with progress percentage, "
                "remaining amount, days until deadline, monthly contribution "
                "needed to hit the target on time, and a status flag "
                "('completed', 'active', 'deadline_passed', 'no_deadline'). "
                "Sorted ascending by progress (least-advanced first). Use this "
                "to answer 'which goal should I focus on?' or 'how close am I "
                "to my goal?'."
            ),
            "parameters": {
                "type": "object",
                "properties": {},
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "compare_months",
            "description": (
                "Compute deltas between two months: income, expenses, balance, "
                "and a per-category breakdown sorted by absolute delta (largest "
                "moves first). Use this when the user asks how a month compares "
                "to another ('como estou esse mês vs mês passado'). More direct "
                "than calling get_transactions twice."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "current_month": {
                        "type": "string",
                        "pattern": "^[0-9]{4}-(0[1-9]|1[0-2])$",
                        "description": "The newer month (YYYY-MM).",
                    },
                    "previous_month": {
                        "type": "string",
                        "pattern": "^[0-9]{4}-(0[1-9]|1[0-2])$",
                        "description": "The older month (YYYY-MM).",
                    },
                },
                "required": ["current_month", "previous_month"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "project_goal_completion",
            "description": (
                "Project when a goal will be reached given a monthly contribution "
                "rate, and compare to the goal's deadline. The agent must supply "
                "the monthly_contribution (e.g. from the user's stated pace, or "
                "from monthlyContributionNeeded surfaced by get_goals). Returns "
                "projected completion date, months to complete, and deadline "
                "status ('ahead_of_deadline', 'exactly_on_deadline', "
                "'after_deadline', or 'no_deadline'). Goal matching is "
                "case-insensitive contains on the title."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "goal_title": {
                        "type": "string",
                        "description": "Full or partial title of the goal (matched case-insensitively).",
                    },
                    "monthly_contribution": {
                        "type": "number",
                        "description": "Monthly amount in BRL the user intends to contribute. Must be positive.",
                    },
                },
                "required": ["goal_title", "monthly_contribution"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "simulate_budget_change",
            "description": (
                "Simulate the impact of spending an additional amount in a "
                "specific category this month. Returns current vs simulated "
                "state with a status flag ('would_stay_within', "
                "'would_be_near_limit', or 'would_overflow'). Use this for "
                "'posso gastar mais R$X em Y?' or any forward-looking spending "
                "decision. Category matching is case-insensitive contains."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "category": {
                        "type": "string",
                        "description": "Name (or partial name) of the budgeted category.",
                    },
                    "additional_amount": {
                        "type": "number",
                        "description": "Extra amount in BRL to layer on top of what's already been spent. Must be non-negative.",
                    },
                    "month": {
                        "type": "string",
                        "pattern": "^[0-9]{4}-(0[1-9]|1[0-2])$",
                        "description": "Target month in YYYY-MM format (e.g. '2026-05').",
                    },
                },
                "required": ["category", "additional_amount", "month"],
            },
        },
    },
]
