import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AiFeedbackResponse } from '../models/ai-feedback.model';
import { AiFeedbackService } from '../services/ai-feedback.service';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class AiFeedbackStore {
  private readonly _feedbacks$ = new BehaviorSubject<AiFeedbackResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');
  private readonly _unreadCount$ = new BehaviorSubject<number>(0);

  readonly feedbacks$ = this._feedbacks$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();
  readonly unreadCount$ = this._unreadCount$.asObservable();

  constructor(
    private readonly feedbackService: AiFeedbackService,
    private readonly toastService: ToastService,
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

  loadUnreadCount(): void {
    this.feedbackService.listAll().subscribe({
      next: (all) => this._unreadCount$.next(all.filter((f) => !f.read).length),
      error: () => {},
    });
  }

  markAsRead(id: string): void {
    const previousList = this._feedbacks$.value;
    const previousCount = this._unreadCount$.value;
    const target = previousList.find((f) => f.id === id);
    const wasUnread = target ? !target.read : false;

    this._feedbacks$.next(previousList.map((f) => (f.id === id ? { ...f, read: true } : f)));
    if (wasUnread && previousCount > 0) {
      this._unreadCount$.next(previousCount - 1);
    }

    this.feedbackService.markAsRead(id).subscribe({
      error: () => {
        this._feedbacks$.next(previousList);
        this._unreadCount$.next(previousCount);
      },
    });
  }
}
