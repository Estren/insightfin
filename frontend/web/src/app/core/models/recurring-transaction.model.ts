import { TransactionType } from './transaction.model';

export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface RecurringTransactionResponse {
  id: string;
  categoryId: string;
  categoryName: string;
  type: TransactionType;
  amount: number;
  description: string | null;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate: string | null;
  nextOccurrence: string;
  lastGeneratedAt: string | null;
  paused: boolean;
  createdAt: string;
}

export interface CreateRecurringTransactionRequest {
  categoryId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate?: string;
}

export interface UpdateRecurringTransactionRequest extends CreateRecurringTransactionRequest {}
