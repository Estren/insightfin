import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AiFeedbackResponse } from '../models/ai-feedback.model';
import { AiFeedbackService } from '../services/ai-feedback.service';

@Injectable({ providedIn: 'root' })
export class AiFeedbackStore {
  private readonly _feedbacks$ = new BehaviorSubject<AiFeedbackResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly feedbacks$ = this._feedbacks$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(private readonly feedbackService: AiFeedbackService) {}

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
      },
    });
  }

  markAsRead(id: string): void {
    // Optimistic update — revert if the request fails
    const previous = this._feedbacks$.value;
    this._feedbacks$.next(previous.map((f) => (f.id === id ? { ...f, read: true } : f)));

    this.feedbackService.markAsRead(id).subscribe({
      error: () => this._feedbacks$.next(previous),
    });
  }
}
