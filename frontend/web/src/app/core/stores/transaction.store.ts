import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import {
  CreateTransactionRequest,
  TransactionResponse,
  UpdateTransactionRequest,
} from '../models/transaction.model';
import { TransactionService } from '../services/transaction.service';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class TransactionStore {
  private readonly _transactions$ = new BehaviorSubject<TransactionResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');
  private readonly _selectedMonth$ = new BehaviorSubject<string>(TransactionStore.currentMonth());

  readonly transactions$ = this._transactions$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();
  readonly selectedMonth$ = this._selectedMonth$.asObservable();

  constructor(
    private readonly transactionService: TransactionService,
    private readonly toastService: ToastService,
  ) {}

  get selectedMonth(): string {
    return this._selectedMonth$.value;
  }

  load(month: string): void {
    this._selectedMonth$.next(month);
    const { startDate, endDate } = TransactionStore.monthRange(month);
    this._loading$.next(true);
    this._error$.next('');

    this.transactionService.list(startDate, endDate).subscribe({
      next: (transactions) => {
        this._transactions$.next(transactions);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load transactions.');
        this._loading$.next(false);
        this.toastService.error('toast.transactions.loadError');
      },
    });
  }

  create(request: CreateTransactionRequest): Observable<TransactionResponse> {
    return this.transactionService.create(request).pipe(
      tap((created) => {
        if (created.date.startsWith(this.selectedMonth)) {
          this._transactions$.next([...this._transactions$.value, created]);
        }
        this.toastService.success('toast.transactions.created');
      }),
      catchError((err) => {
        this.toastService.error('toast.transactions.createError');
        return throwError(() => err);
      }),
    );
  }

  update(id: string, request: UpdateTransactionRequest): Observable<TransactionResponse> {
    return this.transactionService.update(id, request).pipe(
      tap((updated) => {
        const list = this._transactions$.value;
        if (updated.date.startsWith(this.selectedMonth)) {
          const next = list.some((t) => t.id === id)
            ? list.map((t) => (t.id === id ? updated : t))
            : [...list, updated];
          this._transactions$.next(next);
        } else {
          this._transactions$.next(list.filter((t) => t.id !== id));
        }
        this.toastService.success('toast.transactions.updated');
      }),
      catchError((err) => {
        this.toastService.error('toast.transactions.updateError');
        return throwError(() => err);
      }),
    );
  }

  delete(id: string): Observable<void> {
    return this.transactionService.delete(id).pipe(
      tap(() => {
        this._transactions$.next(this._transactions$.value.filter((t) => t.id !== id));
        this.toastService.success('toast.transactions.deleted');
      }),
      catchError((err) => {
        this.toastService.error('toast.transactions.deleteError');
        return throwError(() => err);
      }),
    );
  }

  findById(id: string): TransactionResponse | undefined {
    return this._transactions$.value.find((t) => t.id === id);
  }

  private static currentMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }

  private static monthRange(month: string): { startDate: string; endDate: string } {
    const [year, monthNum] = month.split('-').map(Number);
    const start = new Date(year, monthNum - 1, 1);
    const end = new Date(year, monthNum, 0);
    const format = (d: Date) =>
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    return { startDate: format(start), endDate: format(end) };
  }
}
