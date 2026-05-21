import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { AnalyticsService } from '../services/analytics.service';
import { CategoryService } from '../services/category.service';
import { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '../models/category.model';
import { TransactionType } from '../models/transaction.model';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class CategoryStore {
  private readonly _categories$ = new BehaviorSubject<CategoryResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly categories$ = this._categories$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(
    private readonly categoryService: CategoryService,
    private readonly toastService: ToastService,
    private readonly analytics: AnalyticsService,
  ) {}

  load(type?: TransactionType): void {
    this._loading$.next(true);
    this._error$.next('');

    this.categoryService.list(type).subscribe({
      next: (categories) => {
        this._categories$.next(categories);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load categories.');
        this._loading$.next(false);
        this.toastService.error('toast.categories.loadError');
      },
    });
  }

  create(request: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.categoryService.create(request).pipe(
      tap((created) => {
        this._categories$.next([...this._categories$.value, created]);
        this.toastService.success('toast.categories.created');
        this.analytics.capture('category_created', { type: created.type });
      }),
      catchError((err) => {
        this.toastService.error('toast.categories.createError');
        return throwError(() => err);
      }),
    );
  }

  update(id: string, request: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.categoryService.update(id, request).pipe(
      tap((updated) => {
        const list = this._categories$.value.map((c) => (c.id === id ? updated : c));
        this._categories$.next(list);
        this.toastService.success('toast.categories.updated');
      }),
      catchError((err) => {
        this.toastService.error('toast.categories.updateError');
        return throwError(() => err);
      }),
    );
  }

  delete(id: string): Observable<void> {
    return this.categoryService.delete(id).pipe(
      tap(() => {
        this._categories$.next(this._categories$.value.filter((c) => c.id !== id));
        this.toastService.success('toast.categories.deleted');
      }),
      catchError((err) => {
        this.toastService.error('toast.categories.deleteError');
        return throwError(() => err);
      }),
    );
  }
}
