import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { CategoryService } from '../services/category.service';
import { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '../models/category.model';
import { TransactionType } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class CategoryStore {
  private readonly _categories$ = new BehaviorSubject<CategoryResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly categories$ = this._categories$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(private readonly categoryService: CategoryService) {}

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
      },
    });
  }

  create(request: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.categoryService.create(request).pipe(
      tap((created) => {
        this._categories$.next([...this._categories$.value, created]);
      }),
    );
  }

  update(id: string, request: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.categoryService.update(id, request).pipe(
      tap((updated) => {
        const list = this._categories$.value.map((c) => (c.id === id ? updated : c));
        this._categories$.next(list);
      }),
    );
  }

  delete(id: string): Observable<void> {
    return this.categoryService.delete(id).pipe(
      tap(() => {
        this._categories$.next(this._categories$.value.filter((c) => c.id !== id));
      }),
    );
  }
}
