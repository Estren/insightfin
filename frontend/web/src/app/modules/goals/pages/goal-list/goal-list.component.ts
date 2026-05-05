import { AsyncPipe, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { GoalResponse } from '../../../../core/models/goal.model';
import { GoalStore } from '../../../../core/stores/goal.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-goal-list',
  templateUrl: './goal-list.component.html',
  imports: [AsyncPipe, CurrencyPipe, DatePipe, DecimalPipe, RouterLink, CardComponent, PageHeaderComponent, EmptyStateComponent, LoadingComponent],
})
export class GoalListComponent implements OnInit {
  constructor(
    public readonly goalStore: GoalStore,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.goalStore.load();
  }

  onEdit(goal: GoalResponse): void {
    this.router.navigate(['/goals', goal.id, 'edit']);
  }

  onContribute(goal: GoalResponse): void {
    this.router.navigate(['/goals', goal.id, 'contribute']);
  }

  onDelete(goal: GoalResponse): void {
    const confirmed = window.confirm(`Delete goal "${goal.title}"? This cannot be undone.`);
    if (!confirmed) return;
    this.goalStore.delete(goal.id).subscribe();
  }

  progressPercent(goal: GoalResponse): number {
    if (goal.targetAmount <= 0) return 0;
    const pct = (goal.currentAmount / goal.targetAmount) * 100;
    return pct > 100 ? 100 : pct;
  }
}
