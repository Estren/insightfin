import { CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
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

// All notification kinds share one palette badge; the label text distinguishes them.
const BADGE_CLASS = 'bg-primary/10 text-primary';

@Component({
  selector: 'app-notification-card',
  templateUrl: './notification-card.component.html',
  imports: [CurrencyPipe, DatePipe, NgClass, TranslateModule],
  host: { class: 'block' },
})
export class NotificationCardComponent {
  private readonly store = inject(NotificationStore);

  notification = input.required<NotificationResponse>();
  /** Compact mode disables the inline expand of AI content — used in the navbar popover where space is tight. */
  compact = input<boolean>(false);
  /** When true, the card mounts already expanded. Used by deep-links from the notification dropdown. */
  defaultExpanded = input<boolean>(false);

  readonly expanded = signal(false);

  private readonly _autoExpand = effect(() => {
    if (this.defaultExpanded() && !this.compact() && this.notification().kind === 'AI_FEEDBACK') {
      this.expanded.set(true);
    }
  });

  get badgeClass(): string {
    return BADGE_CLASS;
  }

  get labelKey(): string {
    if (this.notification().kind === 'AI_FEEDBACK' && this.notification().aiFeedbackType) {
      return AI_LABEL_KEYS[this.notification().aiFeedbackType as AiFeedbackType];
    }
    return 'notifications.kinds.budgetAlert';
  }

  toggle(): void {
    // Budget alerts have no extra content to reveal — clicking only marks them read.
    // Compact mode also skips the expand (popover is too narrow for the AI content).
    if (this.notification().kind === 'AI_FEEDBACK' && !this.compact()) {
      this.expanded.set(!this.expanded());
    }
    if (!this.notification().read) {
      this.store.markAsRead(this.notification());
    }
  }
}
