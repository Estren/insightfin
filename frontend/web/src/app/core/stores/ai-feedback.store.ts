import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AiFeedbackResponse } from '../models/ai-feedback.model';
import { AiFeedbackService } from '../services/ai-feedback.service';
import { AnalyticsService } from '../services/analytics.service';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class AiFeedbackStore {
  private readonly _feedbacks$ = new BehaviorSubject<AiFeedbackResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly feedbacks$ = this._feedbacks$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(
    private readonly feedbackService: AiFeedbackService,
    private readonly toastService: ToastService,
    private readonly analytics: AnalyticsService,
  ) {}

  load(month: string): void {
    this._loading$.next(true);
    this._error$.next('');

    this.feedbackService.list(month).subscribe({
      next: (feedbacks) => {
        this._feedbacks$.next(feedbacks);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load AI insights.');
        this._loading$.next(false);
        this.toastService.error('toast.aiFeedback.loadError');
      },
    });
  }

  markAsRead(id: string): void {
    const previousList = this._feedbacks$.value;
    const target = previousList.find((f) => f.id === id);
    const wasUnread = target ? !target.read : false;

    this._feedbacks$.next(previousList.map((f) => (f.id === id ? { ...f, read: true } : f)));

    this.feedbackService.markAsRead(id).subscribe({
      next: () => {
        if (wasUnread) {
          this.analytics.capture('feedback_marked_read');
        }
      },
      error: () => {
        this._feedbacks$.next(previousList);
      },
    });
  }
}
