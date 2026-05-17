import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import {
  CreateRecurringTransactionRequest,
  RecurringTransactionResponse,
  UpdateRecurringTransactionRequest,
} from '../models/recurring-transaction.model';
import { RecurringTransactionService } from '../services/recurring-transaction.service';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class RecurringTransactionStore {
  private readonly _items$ = new BehaviorSubject<RecurringTransactionResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly items$ = this._items$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(
    private readonly service: RecurringTransactionService,
    private readonly toastService: ToastService,
  ) {}

  load(): void {
    this._loading$.next(true);
    this._error$.next('');

    this.service.list().subscribe({
      next: (items) => {
        this._items$.next(items);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load recurring transactions.');
        this._loading$.next(false);
        this.toastService.error('toast.recurring.loadError');
      },
    });
  }

  create(request: CreateRecurringTransactionRequest): Observable<RecurringTransactionResponse> {
    return this.service.create(request).pipe(
      tap((created) => {
        this._items$.next([created, ...this._items$.value]);
        this.toastService.success('toast.recurring.created');
      }),
      catchError((err) => {
        this.toastService.error('toast.recurring.createError');
        return throwError(() => err);
      }),
    );
  }

  update(id: string, request: UpdateRecurringTransactionRequest): Observable<RecurringTransactionResponse> {
    return this.service.update(id, request).pipe(
      tap((updated) => {
        this._items$.next(this._items$.value.map((r) => (r.id === id ? updated : r)));
        this.toastService.success('toast.recurring.updated');
      }),
      catchError((err) => {
        this.toastService.error('toast.recurring.updateError');
        return throwError(() => err);
      }),
    );
  }

  delete(id: string): Observable<void> {
    return this.service.delete(id).pipe(
      tap(() => {
        this._items$.next(this._items$.value.filter((r) => r.id !== id));
        this.toastService.success('toast.recurring.deleted');
      }),
      catchError((err) => {
        this.toastService.error('toast.recurring.deleteError');
        return throwError(() => err);
      }),
    );
  }

  pause(id: string): Observable<RecurringTransactionResponse> {
    return this.service.pause(id).pipe(
      tap((updated) => {
        this._items$.next(this._items$.value.map((r) => (r.id === id ? updated : r)));
        this.toastService.success('toast.recurring.paused');
      }),
      catchError((err) => {
        this.toastService.error('toast.recurring.pauseError');
        return throwError(() => err);
      }),
    );
  }

  resume(id: string): Observable<RecurringTransactionResponse> {
    return this.service.resume(id).pipe(
      tap((updated) => {
        this._items$.next(this._items$.value.map((r) => (r.id === id ? updated : r)));
        this.toastService.success('toast.recurring.resumed');
      }),
      catchError((err) => {
        this.toastService.error('toast.recurring.resumeError');
        return throwError(() => err);
      }),
    );
  }
}
