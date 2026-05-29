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

// All feedback types share one palette badge; the label text distinguishes them.
const TYPE_BADGE_CLASS = 'bg-primary/10 text-primary';

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
    return TYPE_BADGE_CLASS;
  }

  toggle(): void {
    const willExpand = !this.expanded();
    this.expanded.set(willExpand);
    if (willExpand && !this.feedback().read) {
      this.store.markAsRead(this.feedback().id);
    }
  }
}
