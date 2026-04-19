import { Injectable } from '@angular/core';
import { BehaviorSubject, forkJoin, Observable, tap } from 'rxjs';
import {
  BudgetResponse,
  BudgetStatusResponse,
  CreateBudgetRequest,
  UpdateBudgetRequest,
} from '../models/budget.model';
import { BudgetService } from '../services/budget.service';

@Injectable({ providedIn: 'root' })
export class BudgetStore {
  private readonly _budgets$ = new BehaviorSubject<BudgetResponse[]>([]);
  private readonly _statuses$ = new BehaviorSubject<BudgetStatusResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');
  private readonly _selectedMonth$ = new BehaviorSubject<string>(BudgetStore.currentMonth());

  readonly budgets$ = this._budgets$.asObservable();
  readonly statuses$ = this._statuses$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();
  readonly selectedMonth$ = this._selectedMonth$.asObservable();

  constructor(private readonly budgetService: BudgetService) {}

  get selectedMonth(): string {
    return this._selectedMonth$.value;
  }

  load(month: string): void {
    this._selectedMonth$.next(month);
    this._loading$.next(true);
    this._error$.next('');

    forkJoin({
      budgets: this.budgetService.list(month),
      statuses: this.budgetService.getStatus(month),
    }).subscribe({
      next: ({ budgets, statuses }) => {
        this._budgets$.next(budgets);
        this._statuses$.next(statuses);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load budgets.');
        this._loading$.next(false);
      },
    });
  }

  create(request: CreateBudgetRequest): Observable<BudgetResponse> {
    return this.budgetService.create(request).pipe(
      tap(() => this.load(this._selectedMonth$.value)),
    );
  }

  update(id: string, request: UpdateBudgetRequest): Observable<BudgetResponse> {
    return this.budgetService.update(id, request).pipe(
      tap(() => this.load(this._selectedMonth$.value)),
    );
  }

  delete(id: string): Observable<void> {
    return this.budgetService.delete(id).pipe(
      tap(() => this.load(this._selectedMonth$.value)),
    );
  }

  findById(id: string): BudgetResponse | undefined {
    return this._budgets$.value.find((b) => b.id === id);
  }

  private static currentMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }
}
