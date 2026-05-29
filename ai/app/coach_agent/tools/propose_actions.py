"""Write-action *proposals* for the Coach.

These functions never mutate anything. They validate/normalize the LLM's
arguments and return a structured proposal that the streaming layer surfaces to
the user as a confirmation card (an ``action_proposal`` SSE event). The actual
mutation happens only in core-api, after the user explicitly confirms, against
their JWT-authenticated identity — the LLM never touches the write path.
"""

from __future__ import annotations


class ProposalError(ValueError):
    """Raised when a proposed action's arguments are invalid."""


def propose_create_budget(category: str, amount: float) -> dict:
    """Build a proposal to create a monthly budget for a category.

    Validates only what's intrinsic to the args (non-empty category, positive
    amount). The category's existence and duplicate checks are enforced
    server-side by core-api at execution time — this stays a pure proposal.
    """
    name = (category or "").strip()
    if not name:
        raise ProposalError("category is required")
    if amount is None or amount <= 0:
        raise ProposalError("amount must be a positive number")

    return {
        "status": "awaiting_user_confirmation",
        "action": "create_budget",
        "params": {"category": name, "amount": round(float(amount), 2)},
        "summary": f"Criar orçamento de R$ {amount:.2f} para {name} (mês atual)",
    }


def propose_contribute_goal(goal_title: str, amount: float) -> dict:
    """Build a proposal to contribute an amount to one of the user's goals.

    The goal's existence is resolved server-side (by title) at execution time;
    this stays a pure proposal that validates only the intrinsic args.
    """
    title = (goal_title or "").strip()
    if not title:
        raise ProposalError("goal_title is required")
    if amount is None or amount <= 0:
        raise ProposalError("amount must be a positive number")

    return {
        "status": "awaiting_user_confirmation",
        "action": "contribute_goal",
        "params": {"goal_title": title, "amount": round(float(amount), 2)},
        "summary": f'Contribuir R$ {amount:.2f} para a meta "{title}"',
    }


def propose_create_goal(title: str, target_amount: float, deadline: str | None = None) -> dict:
    """Build a proposal to create a savings goal.

    ``deadline`` is optional (goals can have none) and, when given, must be an
    ISO date (YYYY-MM-DD); it's validated server-side at execution time.
    """
    name = (title or "").strip()
    if not name:
        raise ProposalError("title is required")
    if target_amount is None or target_amount <= 0:
        raise ProposalError("target_amount must be a positive number")

    clean_deadline = (deadline or "").strip() or None
    when = f" até {clean_deadline}" if clean_deadline else " (sem prazo)"
    return {
        "status": "awaiting_user_confirmation",
        "action": "create_goal",
        "params": {"title": name, "target_amount": round(float(target_amount), 2), "deadline": clean_deadline},
        "summary": f'Criar a meta "{name}" de R$ {target_amount:.2f}{when}',
    }


def propose_adjust_budget(category: str, amount: float) -> dict:
    """Build a proposal to change an existing budget's monthly limit."""
    name = (category or "").strip()
    if not name:
        raise ProposalError("category is required")
    if amount is None or amount <= 0:
        raise ProposalError("amount must be a positive number")

    return {
        "status": "awaiting_user_confirmation",
        "action": "adjust_budget",
        "params": {"category": name, "amount": round(float(amount), 2)},
        "summary": f"Ajustar o orçamento de {name} para R$ {amount:.2f} (mês atual)",
    }


def propose_log_transaction(
    type: str, category: str, amount: float, description: str | None = None
) -> dict:
    """Build a proposal to log an income/expense transaction for today."""
    normalized_type = (type or "").strip().upper()
    if normalized_type not in ("INCOME", "EXPENSE"):
        raise ProposalError("type must be INCOME or EXPENSE")
    name = (category or "").strip()
    if not name:
        raise ProposalError("category is required")
    if amount is None or amount <= 0:
        raise ProposalError("amount must be a positive number")

    clean_description = (description or "").strip() or None
    kind = "receita" if normalized_type == "INCOME" else "despesa"
    return {
        "status": "awaiting_user_confirmation",
        "action": "log_transaction",
        "params": {
            "type": normalized_type,
            "category": name,
            "amount": round(float(amount), 2),
            "description": clean_description,
        },
        "summary": f"Registrar {kind} de R$ {amount:.2f} em {name} (hoje)",
    }
