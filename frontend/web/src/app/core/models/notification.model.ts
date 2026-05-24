import { AiFeedbackType } from './ai-feedback.model';

export type NotificationKind = 'AI_FEEDBACK' | 'BUDGET_ALERT';

export interface NotificationResponse {
  id: string;
  kind: NotificationKind;
  read: boolean;
  createdAt: string;

  // AI_FEEDBACK fields (null when kind=BUDGET_ALERT)
  aiFeedbackType: AiFeedbackType | null;
  title: string | null;
  content: string | null;
  referenceMonth: string | null;

  // BUDGET_ALERT fields (null when kind=AI_FEEDBACK)
  budgetId: string | null;
  categoryName: string | null;
  thresholdPercentage: number | null;
  amountSpent: number | null;
  budgetAmount: number | null;
  triggeredAt: string | null;
}

export interface UnreadCountResponse {
  aiFeedbacks: number;
  budgetAlerts: number;
  total: number;
}

export interface NotificationPage {
  items: NotificationResponse[];
  nextCursor: string | null;
  hasMore: boolean;
}
