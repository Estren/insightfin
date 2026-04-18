import { TransactionType } from './transaction.model';

export interface CategoryResponse {
  id: string;
  name: string;
  type: TransactionType;
  icon: string | null;
  color: string | null;
  createdAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  type: TransactionType;
  icon?: string;
  color?: string;
}

export interface UpdateCategoryRequest {
  name: string;
  type: TransactionType;
  icon?: string;
  color?: string;
}
