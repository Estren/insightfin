export type TransactionType = 'INCOME' | 'EXPENSE';

export interface TransactionResponse {
  id: string;
  categoryId: string;
  categoryName: string;
  type: TransactionType;
  amount: number;
  description: string | null;
  date: string;
  recurringTransactionId: string | null;
  createdAt: string;
}

export interface CreateTransactionRequest {
  categoryId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  date: string;
}

export interface UpdateTransactionRequest {
  categoryId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  date: string;
}
