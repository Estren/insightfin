import { BudgetStatusResponse } from './budget.model';
import { GoalResponse } from './goal.model';
import { TransactionResponse } from './transaction.model';

export interface DashboardResponse {
  totalIncome: number;
  totalExpense: number;
  balance: number;
  recentTransactions: TransactionResponse[];
  activeGoals: GoalResponse[];
  budgetStatuses: BudgetStatusResponse[];
}
