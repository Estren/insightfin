import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { NotificationResponse, UnreadCountResponse } from '../models/notification.model';
import { NotificationService } from '../services/notification.service';
import { AnalyticsService } from '../services/analytics.service';
import { ToastService } from '../services/toast.service';

const EMPTY_COUNTS: UnreadCountResponse = { aiFeedbacks: 0, budgetAlerts: 0, total: 0 };

@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private readonly _notifications$ = new BehaviorSubject<NotificationResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');
  private readonly _counts$ = new BehaviorSubject<UnreadCountResponse>(EMPTY_COUNTS);

  readonly notifications$ = this._notifications$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();
  readonly counts$ = this._counts$.asObservable();

  constructor(
    private readonly notificationService: NotificationService,
    private readonly toastService: ToastService,
    private readonly analytics: AnalyticsService,
  ) {}

  load(): void {
    this._loading$.next(true);
    this._error$.next('');

    this.notificationService.list().subscribe({
      next: (notifications) => {
        this._notifications$.next(notifications);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load notifications.');
        this._loading$.next(false);
        this.toastService.error('toast.notifications.loadError');
      },
    });
  }

  loadUnreadCount(): void {
    this.notificationService.unreadCount().subscribe({
      next: (counts) => this._counts$.next(counts),
      error: () => {},
    });
  }

  markAsRead(notification: NotificationResponse): void {
    if (notification.read) return;

    const previousList = this._notifications$.value;
    const previousCounts = this._counts$.value;

    // Optimistic update — flip read=true on the matching item and decrement the
    // right counter. Server failure rolls both back.
    this._notifications$.next(previousList.map((n) => (n.id === notification.id ? { ...n, read: true } : n)));
    this._counts$.next(this.decrementCount(previousCounts, notification.kind));

    const request$ =
      notification.kind === 'AI_FEEDBACK'
        ? this.notificationService.markAiFeedbackAsRead(notification.id)
        : this.notificationService.markBudgetAlertAsRead(notification.id);

    request$.subscribe({
      next: () => this.analytics.capture('notification_marked_read', { kind: notification.kind }),
      error: () => {
        this._notifications$.next(previousList);
        this._counts$.next(previousCounts);
      },
    });
  }

  private decrementCount(counts: UnreadCountResponse, kind: NotificationResponse['kind']): UnreadCountResponse {
    if (kind === 'AI_FEEDBACK' && counts.aiFeedbacks > 0) {
      return { ...counts, aiFeedbacks: counts.aiFeedbacks - 1, total: counts.total - 1 };
    }
    if (kind === 'BUDGET_ALERT' && counts.budgetAlerts > 0) {
      return { ...counts, budgetAlerts: counts.budgetAlerts - 1, total: counts.total - 1 };
    }
    return counts;
  }
}
