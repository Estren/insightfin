import { AiFeedbackResponse } from './ai-feedback.model';

export interface HealthScoreBreakdown {
  savingsRate: number;
  budgetAdherence: number;
  goalProgress: number;
  expenseConsistency: number;
}

export interface HealthScoreMetadata {
  score: number;
  breakdown: HealthScoreBreakdown;
}

/**
 * Pulls the most recent HEALTH_SCORE feedback from a list and parses its
 * metadata JSON. Returns {@code null} if no such feedback exists or the
 * metadata isn't shaped the way the AI service produces it — calls sites
 * should treat that as "no score available yet" and show an empty state.
 */
export function extractLatestHealthScore(feedbacks: AiFeedbackResponse[]): HealthScoreMetadata | null {
  const scored = feedbacks
    .filter((f) => f.type === 'HEALTH_SCORE' && f.metadata)
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt));

  for (const feedback of scored) {
    try {
      const parsed = JSON.parse(feedback.metadata as string);
      if (
        typeof parsed?.score === 'number' &&
        parsed.breakdown &&
        typeof parsed.breakdown.savingsRate === 'number' &&
        typeof parsed.breakdown.budgetAdherence === 'number' &&
        typeof parsed.breakdown.goalProgress === 'number' &&
        typeof parsed.breakdown.expenseConsistency === 'number'
      ) {
        return parsed as HealthScoreMetadata;
      }
    } catch {
      // Malformed metadata — try the next one.
    }
  }
  return null;
}
