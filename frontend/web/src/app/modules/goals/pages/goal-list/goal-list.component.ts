import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ConfirmDialogService } from '../../../../core/services/confirm-dialog.service';
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
    private readonly confirmDialog: ConfirmDialogService,
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
    this.confirmDialog
      .confirm({
        title: 'common.deleteTitle',
        message: 'common.deleteConfirm',
        messageParams: { name: goal.title },
        confirmLabel: 'common.delete',
        variant: 'danger',
      })
      .subscribe((confirmed) => {
        if (confirmed) this.goalStore.delete(goal.id).subscribe();
      });
  }
}
