import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { DashboardService } from '../services/dashboard.service';
import { DashboardResponse } from '../models/dashboard.model';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly _summary$ = new BehaviorSubject<DashboardResponse | null>(null);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly summary$ = this._summary$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly toastService: ToastService,
  ) {}

  load(month: string): void {
    this._loading$.next(true);
    this._error$.next('');

    this.dashboardService.get(month).subscribe({
      next: (summary) => {
        this._summary$.next(summary);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load dashboard data.');
        this._loading$.next(false);
        this.toastService.error('toast.dashboard.loadError');
      },
    });
  }
}
