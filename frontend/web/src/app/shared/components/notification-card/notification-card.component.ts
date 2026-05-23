import { CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AiFeedbackType } from '../../../core/models/ai-feedback.model';
import { NotificationResponse } from '../../../core/models/notification.model';
import { NotificationStore } from '../../../core/stores/notification.store';

const AI_LABEL_KEYS: Record<AiFeedbackType, string> = {
  MONTHLY_REPORT: 'dashboard.feedbackTypes.monthlyReport',
  HEALTH_SCORE: 'dashboard.feedbackTypes.healthScore',
  ALERT: 'dashboard.feedbackTypes.alert',
  GOAL_PROJECTION: 'dashboard.feedbackTypes.goalProjection',
};

const AI_BADGE_CLASSES: Record<AiFeedbackType, string> = {
  MONTHLY_REPORT: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  HEALTH_SCORE: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  ALERT: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
  GOAL_PROJECTION: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
};

const BUDGET_BADGE_CLASS = 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400';

@Component({
  selector: 'app-notification-card',
  templateUrl: './notification-card.component.html',
  imports: [CurrencyPipe, DatePipe, NgClass, TranslateModule],
})
export class NotificationCardComponent {
  private readonly store = inject(NotificationStore);

  notification = input.required<NotificationResponse>();

  readonly expanded = signal(false);

  get badgeClass(): string {
    if (this.notification().kind === 'AI_FEEDBACK' && this.notification().aiFeedbackType) {
      return AI_BADGE_CLASSES[this.notification().aiFeedbackType as AiFeedbackType];
    }
    return BUDGET_BADGE_CLASS;
  }

  get labelKey(): string {
    if (this.notification().kind === 'AI_FEEDBACK' && this.notification().aiFeedbackType) {
      return AI_LABEL_KEYS[this.notification().aiFeedbackType as AiFeedbackType];
    }
    return 'notifications.kinds.budgetAlert';
  }

  toggle(): void {
    // Budget alerts have no extra content to reveal — clicking only marks them read.
    if (this.notification().kind === 'AI_FEEDBACK') {
      this.expanded.set(!this.expanded());
    }
    if (!this.notification().read) {
      this.store.markAsRead(this.notification());
    }
  }
}
