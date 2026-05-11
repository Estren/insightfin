import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { GoalResponse } from '../../../../core/models/goal.model';
import { GoalStore } from '../../../../core/stores/goal.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ProgressRingComponent } from '../../../../shared/components/progress-ring/progress-ring.component';

@Component({
  selector: 'app-goal-list',
  templateUrl: './goal-list.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    DatePipe,
    RouterLink,
    RouterOutlet,
    TranslateModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingComponent,
    ProgressRingComponent,
  ],
})
export class GoalListComponent implements OnInit {
  constructor(
    public readonly goalStore: GoalStore,
    private readonly router: Router,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.goalStore.load();
  }

  progressPercent(goal: GoalResponse): number {
    if (goal.targetAmount <= 0) return 0;
    const pct = (goal.currentAmount / goal.targetAmount) * 100;
    return pct > 100 ? 100 : pct;
  }

  onEdit(goal: GoalResponse): void {
    this.router.navigate(['/goals', goal.id, 'edit']);
  }

  onContribute(goal: GoalResponse): void {
    this.router.navigate(['/goals', goal.id, 'contribute']);
  }

  onDelete(goal: GoalResponse): void {
    const msg = this.translate.instant('common.deleteConfirm', { name: goal.title });
    if (!window.confirm(msg)) return;
    this.goalStore.delete(goal.id).subscribe();
  }
}
