import { Component, OnInit } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardStore } from '../../../../core/stores/dashboard.store';
import { AiFeedbackStore } from '../../../../core/stores/ai-feedback.store';
import { AiFeedbackType, AiFeedbackResponse } from '../../../../core/models/ai-feedback.model';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
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
    TranslateModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingComponent,
    StatCardComponent,
  ],
})
export class OverviewComponent implements OnInit {
  expandedId: string | null = null;

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

  toggleFeedback(feedback: AiFeedbackResponse): void {
    this.expandedId = this.expandedId === feedback.id ? null : feedback.id;
    if (!feedback.read && this.expandedId === feedback.id) {
      this.feedbackStore.markAsRead(feedback.id);
    }
  }

  feedbackTypeKey(type: AiFeedbackType): string {
    const keys: Record<AiFeedbackType, string> = {
      MONTHLY_REPORT: 'dashboard.feedbackTypes.monthlyReport',
      HEALTH_SCORE: 'dashboard.feedbackTypes.healthScore',
      ALERT: 'dashboard.feedbackTypes.alert',
      GOAL_PROJECTION: 'dashboard.feedbackTypes.goalProjection',
    };
    return keys[type];
  }

  feedbackTypeClass(type: AiFeedbackType): string {
    const classes: Record<AiFeedbackType, string> = {
      MONTHLY_REPORT: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
      HEALTH_SCORE: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
      ALERT: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
      GOAL_PROJECTION: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
    };
    return classes[type];
  }
}
