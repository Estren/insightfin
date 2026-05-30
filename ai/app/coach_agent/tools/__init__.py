from app.coach_agent.tools.compare_months import compare_months
from app.coach_agent.tools.get_budget_status import get_budget_status
from app.coach_agent.tools.get_goals import get_goals
from app.coach_agent.tools.get_health_score import get_health_score
from app.coach_agent.tools.get_transactions import get_transactions
from app.coach_agent.tools.present_chart import (
    ChartError,
    present_donut_chart,
    present_line_chart,
)
from app.coach_agent.tools.project_goal_completion import project_goal_completion
from app.coach_agent.tools.propose_actions import (
    ProposalError,
    propose_adjust_budget,
    propose_contribute_goal,
    propose_create_budget,
    propose_create_goal,
    propose_log_transaction,
)
from app.coach_agent.tools.registry import TOOL_DEFINITIONS
from app.coach_agent.tools.simulate_budget_change import simulate_budget_change

__all__ = [
    "compare_months",
    "get_budget_status",
    "get_goals",
    "get_health_score",
    "get_transactions",
    "present_donut_chart",
    "present_line_chart",
    "project_goal_completion",
    "propose_adjust_budget",
    "propose_contribute_goal",
    "propose_create_budget",
    "propose_create_goal",
    "propose_log_transaction",
    "ChartError",
    "ProposalError",
    "simulate_budget_change",
    "TOOL_DEFINITIONS",
]
