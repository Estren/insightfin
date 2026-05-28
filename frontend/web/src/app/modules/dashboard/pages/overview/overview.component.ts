import { Component, OnInit } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { DashboardStore } from '../../../../core/stores/dashboard.store';
import { AiFeedbackStore } from '../../../../core/stores/ai-feedback.store';
import { AiFeedbackResponse } from '../../../../core/models/ai-feedback.model';
import { BudgetStatusResponse } from '../../../../core/models/budget.model';
import { DashboardResponse } from '../../../../core/models/dashboard.model';
import { extractLatestHealthScore, HealthScoreMetadata } from '../../../../core/models/health-score.model';

/** A proactive Coach suggestion shown on the dashboard. The `message*` keys
 *  render the card text (reactive to language); the `question*` keys are
 *  resolved at click time into the prompt sent to the Coach. */
interface CoachPrompt {
  icon: string;
  messageKey: string;
  messageParams?: Record<string, string | number>;
  questionKey: string;
  questionParams?: Record<string, string | number>;
}
import { CardComponent } from '../../../../shared/components/card/card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { FeedbackCardComponent } from '../../../../shared/components/feedback-card/feedback-card.component';
import { HealthScoreGaugeComponent } from '../../../../shared/components/health-score-gauge/health-score-gauge.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    DatePipe,
    NgClass,
    RouterLink,
    TranslateModule,
    AngularSvgIconModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    FeedbackCardComponent,
    HealthScoreGaugeComponent,
    LoadingComponent,
    StatCardComponent,
  ],
})
export class OverviewComponent implements OnInit {
  criticalBudgetsCount(statuses: BudgetStatusResponse[]): number {
    return statuses.filter((b) => b.percentageUsed >= 90).length;
  }

  /** Picks the latest HEALTH_SCORE feedback from the current month, parsed and validated. */
  healthScore(feedbacks: AiFeedbackResponse[]): HealthScoreMetadata | null {
    return extractLatestHealthScore(feedbacks);
  }

  /** Up to two proactive Coach prompts derived from the loaded dashboard data:
   *  the worst over-limit budget, and a low health score. Empty when neither
   *  condition holds, so the section hides itself. */
  coachPrompts(summary: DashboardResponse, feedbacks: AiFeedbackResponse[]): CoachPrompt[] {
    const prompts: CoachPrompt[] = [];

    const over = summary.budgetStatuses
      .filter((b) => b.percentageUsed > 100)
      .sort((a, b) => b.percentageUsed - a.percentageUsed);
    if (over.length > 0) {
      const worst = over[0];
      prompts.push({
        icon: 'assets/icons/heroicons/outline/exclamation-triangle.svg',
        messageKey: 'dashboard.coachPrompts.budgetOver',
        messageParams: { category: worst.categoryName, percent: Math.round(worst.percentageUsed) },
        questionKey: 'dashboard.coachPrompts.budgetOverQuestion',
        questionParams: { category: worst.categoryName },
      });
    }

    const score = this.healthScore(feedbacks);
    if (score && score.score < 50) {
      prompts.push({
        icon: 'assets/icons/heroicons/outline/chat-bubble.svg',
        messageKey: 'dashboard.coachPrompts.lowScore',
        messageParams: { score: score.score },
        questionKey: 'dashboard.coachPrompts.lowScoreQuestion',
      });
    }

    return prompts.slice(0, 2);
  }

  askCoach(prompt: CoachPrompt): void {
    const question = this.translate.instant(prompt.questionKey, prompt.questionParams);
    this.router.navigate(['/coach'], { state: { question } });
  }

  constructor(
    public readonly dashboardStore: DashboardStore,
    public readonly feedbackStore: AiFeedbackStore,
    private readonly router: Router,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    const now = new Date();
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    this.dashboardStore.load(month);
    this.feedbackStore.load(month);
  }
}
