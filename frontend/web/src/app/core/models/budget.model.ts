export interface BudgetResponse {
  id: string;
  categoryId: string;
  categoryName: string;
  amount: number;
  month: string;
  createdAt: string;
}

export interface BudgetStatusResponse {
  budgetId: string;
  categoryId: string;
  categoryName: string;
  budgetAmount: number;
  spentAmount: number;
  percentageUsed: number;
}

export interface CreateBudgetRequest {
  categoryId: string;
  amount: number;
  month: string;
}

export interface UpdateBudgetRequest {
  amount: number;
}
