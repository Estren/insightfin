export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface GoalResponse {
  id: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
  deadline: string | null;
  status: GoalStatus;
  createdAt: string;
}

export interface GoalContributionResponse {
  id: string;
  goalId: string;
  amount: number;
  date: string;
  createdAt: string;
}

export interface CreateGoalRequest {
  title: string;
  targetAmount: number;
  deadline?: string;
}

export interface UpdateGoalRequest {
  title: string;
  targetAmount: number;
  deadline?: string;
}

export interface CreateGoalContributionRequest {
  amount: number;
  date: string;
}
