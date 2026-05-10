import { AsyncPipe, CurrencyPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { BudgetStatusResponse } from '../../../../core/models/budget.model';
import { BudgetStore } from '../../../../core/stores/budget.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ProgressRingComponent } from '../../../../shared/components/progress-ring/progress-ring.component';

@Component({
  selector: 'app-budget-list',
  templateUrl: './budget-list.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    NgClass,
    RouterLink,
    TranslateModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingComponent,
    ProgressRingComponent,
    RouterOutlet,
  ],
})
export class BudgetListComponent implements OnInit {
  constructor(
    public readonly budgetStore: BudgetStore,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.budgetStore.load(this.budgetStore.selectedMonth);
  }

  changeMonth(delta: number): void {
    const [year, month] = this.budgetStore.selectedMonth.split('-').map(Number);
    const next = new Date(year, month - 1 + delta, 1);
    const nextMonth = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`;
    this.budgetStore.load(nextMonth);
  }

  onEdit(status: BudgetStatusResponse): void {
    this.router.navigate(['/budgets', status.budgetId, 'edit']);
  }

  onDelete(status: BudgetStatusResponse): void {
    const confirmed = window.confirm(`Delete budget for "${status.categoryName}"? This cannot be undone.`);
    if (!confirmed) return;
    this.budgetStore.delete(status.budgetId).subscribe();
  }

  statusKey(pct: number): string {
    if (pct >= 90) return 'budgets.statusOver';
    if (pct >= 70) return 'budgets.statusWarning';
    return 'budgets.statusOnTrack';
  }

  statusClass(pct: number): string {
    if (pct >= 90) return 'bg-red-500/15 text-red-600 dark:text-red-400';
    if (pct >= 70) return 'bg-amber-500/15 text-amber-600 dark:text-amber-400';
    return 'bg-green-500/15 text-green-600 dark:text-green-400';
  }
}
