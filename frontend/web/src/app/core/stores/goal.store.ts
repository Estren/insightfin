import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import {
  CreateGoalContributionRequest,
  CreateGoalRequest,
  GoalContributionResponse,
  GoalResponse,
  UpdateGoalRequest,
} from '../models/goal.model';
import { GoalService } from '../services/goal.service';

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

  constructor(private readonly goalService: GoalService) {}

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
      },
    });
  }

  create(request: CreateGoalRequest): Observable<GoalResponse> {
    return this.goalService.create(request).pipe(tap(() => this.load()));
  }

  update(id: string, request: UpdateGoalRequest): Observable<GoalResponse> {
    return this.goalService.update(id, request).pipe(tap(() => this.load()));
  }

  delete(id: string): Observable<void> {
    return this.goalService.delete(id).pipe(tap(() => this.load()));
  }

  contribute(goalId: string, request: CreateGoalContributionRequest): Observable<GoalContributionResponse> {
    return this.goalService.contribute(goalId, request).pipe(tap(() => this.load()));
  }

  findById(id: string): GoalResponse | undefined {
    return this._goals$.value.find((g) => g.id === id);
  }
}
