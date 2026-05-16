import { NgClass } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AiFeedbackResponse, AiFeedbackType } from '../../../core/models/ai-feedback.model';
import { AiFeedbackStore } from '../../../core/stores/ai-feedback.store';

const TYPE_LABEL_KEYS: Record<AiFeedbackType, string> = {
  MONTHLY_REPORT: 'dashboard.feedbackTypes.monthlyReport',
  HEALTH_SCORE: 'dashboard.feedbackTypes.healthScore',
  ALERT: 'dashboard.feedbackTypes.alert',
  GOAL_PROJECTION: 'dashboard.feedbackTypes.goalProjection',
};

const TYPE_BADGE_CLASSES: Record<AiFeedbackType, string> = {
  MONTHLY_REPORT: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  HEALTH_SCORE: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  ALERT: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
  GOAL_PROJECTION: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
};

@Component({
  selector: 'app-feedback-card',
  templateUrl: './feedback-card.component.html',
  imports: [NgClass, TranslateModule],
})
export class FeedbackCardComponent {
  private readonly store = inject(AiFeedbackStore);

  feedback = input.required<AiFeedbackResponse>();

  readonly expanded = signal(false);

  get labelKey(): string {
    return TYPE_LABEL_KEYS[this.feedback().type];
  }

  get badgeClass(): string {
    return TYPE_BADGE_CLASSES[this.feedback().type];
  }

  toggle(): void {
    const willExpand = !this.expanded();
    this.expanded.set(willExpand);
    if (willExpand && !this.feedback().read) {
      this.store.markAsRead(this.feedback().id);
    }
  }
}
