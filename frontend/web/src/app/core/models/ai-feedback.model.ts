export type AiFeedbackType = 'MONTHLY_REPORT' | 'ALERT' | 'GOAL_PROJECTION' | 'HEALTH_SCORE';

export interface AiFeedbackResponse {
  id: string;
  type: AiFeedbackType;
  title: string;
  content: string;
  metadata: string | null;
  referenceMonth: string | null;
  read: boolean;
  createdAt: string;
}
