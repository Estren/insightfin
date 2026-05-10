import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import {
  CreateGoalContributionRequest,
  CreateGoalRequest,
  GoalContributionResponse,
  GoalResponse,
  UpdateGoalRequest,
} from '../models/goal.model';
import { GoalService } from '../services/goal.service';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class GoalStore {
  private readonly _goals$ = new BehaviorSubject<GoalResponse[]>([]);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly goals$ = this._goals$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  readonly activeGoals$ = this.goals$.pipe(map((goals) => goals.filter((g) => g.status === 'ACTIVE')));
  readonly completedGoals$ = this.goals$.pipe(map((goals) => goals.filter((g) => g.status === 'COMPLETED')));

  constructor(
    private readonly goalService: GoalService,
    private readonly toastService: ToastService,
  ) {}

  load(): void {
    this._loading$.next(true);
    this._error$.next('');
    this.goalService.list().subscribe({
      next: (goals) => {
        this._goals$.next(goals);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load goals.');
        this._loading$.next(false);
        this.toastService.error('toast.goals.loadError');
      },
    });
  }

  create(request: CreateGoalRequest): Observable<GoalResponse> {
    return this.goalService.create(request).pipe(
      tap(() => {
        this.load();
        this.toastService.success('toast.goals.created');
      }),
      catchError((err) => {
        this.toastService.error('toast.goals.createError');
        return throwError(() => err);
      }),
    );
  }

  update(id: string, request: UpdateGoalRequest): Observable<GoalResponse> {
    return this.goalService.update(id, request).pipe(
      tap(() => {
        this.load();
        this.toastService.success('toast.goals.updated');
      }),
      catchError((err) => {
        this.toastService.error('toast.goals.updateError');
        return throwError(() => err);
      }),
    );
  }

  delete(id: string): Observable<void> {
    return this.goalService.delete(id).pipe(
      tap(() => {
        this.load();
        this.toastService.success('toast.goals.deleted');
      }),
      catchError((err) => {
        this.toastService.error('toast.goals.deleteError');
        return throwError(() => err);
      }),
    );
  }

  contribute(goalId: string, request: CreateGoalContributionRequest): Observable<GoalContributionResponse> {
    return this.goalService.contribute(goalId, request).pipe(
      tap(() => {
        this.load();
        this.toastService.success('toast.goals.contributed');
      }),
      catchError((err) => {
        this.toastService.error('toast.goals.contributeError');
        return throwError(() => err);
      }),
    );
  }

  findById(id: string): GoalResponse | undefined {
    return this._goals$.value.find((g) => g.id === id);
  }
}
