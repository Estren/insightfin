import { Component, OnInit } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { DashboardStore } from '../../../../core/stores/dashboard.store';
import { AiFeedbackStore } from '../../../../core/stores/ai-feedback.store';
import { AiFeedbackResponse } from '../../../../core/models/ai-feedback.model';
import { BudgetStatusResponse } from '../../../../core/models/budget.model';
import { extractLatestHealthScore, HealthScoreMetadata } from '../../../../core/models/health-score.model';
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

  constructor(
    public readonly dashboardStore: DashboardStore,
    public readonly feedbackStore: AiFeedbackStore,
  ) {}

  ngOnInit(): void {
    const now = new Date();
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    this.dashboardStore.load(month);
    this.feedbackStore.load(month);
  }
}
