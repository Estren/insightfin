import { AsyncPipe, CurrencyPipe, DecimalPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BudgetStatusResponse } from '../../../../core/models/budget.model';
import { BudgetStore } from '../../../../core/stores/budget.store';

@Component({
  selector: 'app-budget-list',
  templateUrl: './budget-list.component.html',
  imports: [AsyncPipe, CurrencyPipe, DecimalPipe, NgClass, RouterLink],
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

  progressWidth(percentage: number): number {
    return percentage > 100 ? 100 : percentage;
  }
}
