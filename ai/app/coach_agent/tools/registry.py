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
    {
        "type": "function",
        "function": {
            "name": "propose_create_budget",
            "description": (
                "PROPOSE creating a monthly budget for a category. This does NOT "
                "create anything — it surfaces a confirmation card the user must "
                "approve before it takes effect. Use it when the user asks you to "
                "set/create a budget, or clearly agrees to a budget you suggested "
                "(e.g. 'pode criar', 'sim, defina R$500'). The budget applies to "
                "the current month. After calling it, ask the user to confirm in "
                "ONE short sentence; NEVER say the budget was created."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "category": {
                        "type": "string",
                        "description": "Name of an existing category to budget (e.g. 'Alimentação').",
                    },
                    "amount": {
                        "type": "number",
                        "description": "Monthly budget limit in BRL. Must be positive.",
                    },
                },
                "required": ["category", "amount"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "propose_contribute_goal",
            "description": (
                "PROPOSE contributing an amount to one of the user's savings "
                "goals. This does NOT move any money — it surfaces a confirmation "
                "card the user must approve. Use it when the user asks to "
                "add/contribute to a goal, or clearly agrees to a contribution "
                "you suggested. After calling it, ask the user to confirm in ONE "
                "short sentence; NEVER say the contribution was made."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "goal_title": {
                        "type": "string",
                        "description": "Full or partial title of an existing goal (matched case-insensitively).",
                    },
                    "amount": {
                        "type": "number",
                        "description": "Amount in BRL to contribute. Must be positive.",
                    },
                },
                "required": ["goal_title", "amount"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "propose_create_goal",
            "description": (
                "PROPOSE creating a savings goal. This does NOT create anything — "
                "it surfaces a confirmation card the user must approve. Use it "
                "when the user asks to set/create a goal, or agrees to one you "
                "suggested. After calling it, ask the user to confirm in ONE "
                "short sentence; NEVER say the goal was created."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "title": {
                        "type": "string",
                        "description": "Goal title (e.g. 'Reserva de emergência').",
                    },
                    "target_amount": {
                        "type": "number",
                        "description": "Target amount in BRL. Must be positive.",
                    },
                    "deadline": {
                        "type": "string",
                        "pattern": "^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$",
                        "description": "Optional deadline as YYYY-MM-DD. Omit if the user gave none.",
                    },
                },
                "required": ["title", "target_amount"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "propose_adjust_budget",
            "description": (
                "PROPOSE changing the monthly limit of an EXISTING budget for a "
                "category (current month). Does NOT change anything — shows a "
                "confirmation card. Use it when the user wants to raise/lower a "
                "budget they already have (for a brand-new budget use "
                "propose_create_budget). After calling it, ask the user to "
                "confirm in ONE short sentence; NEVER say it was changed."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "category": {
                        "type": "string",
                        "description": "Name of the budgeted category to adjust (e.g. 'Alimentação').",
                    },
                    "amount": {
                        "type": "number",
                        "description": "New monthly budget limit in BRL. Must be positive.",
                    },
                },
                "required": ["category", "amount"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "propose_log_transaction",
            "description": (
                "PROPOSE logging an income or expense transaction for today. Does "
                "NOT record anything — shows a confirmation card. Use it when the "
                "user wants to register something they earned or spent (e.g. "
                "'registra R$80 que gastei no mercado'). After calling it, ask "
                "the user to confirm in ONE short sentence; NEVER say it was "
                "recorded."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "type": {
                        "type": "string",
                        "enum": ["INCOME", "EXPENSE"],
                        "description": "Whether the money came in (INCOME) or went out (EXPENSE).",
                    },
                    "category": {
                        "type": "string",
                        "description": "Name of an existing category of the matching type.",
                    },
                    "amount": {
                        "type": "number",
                        "description": "Amount in BRL. Must be positive.",
                    },
                    "description": {
                        "type": "string",
                        "description": "Optional short note (e.g. 'mercado').",
                    },
                },
                "required": ["type", "category", "amount"],
            },
        },
    },
]
