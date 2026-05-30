import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RecurringTransactionResponse } from '../../../../core/models/recurring-transaction.model';
import { ConfirmDialogService } from '../../../../core/services/confirm-dialog.service';
import { RecurringTransactionStore } from '../../../../core/stores/recurring-transaction.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-recurring-list',
  templateUrl: './recurring-list.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    DatePipe,
    NgClass,
    RouterLink,
    RouterOutlet,
    TranslateModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
})
export class RecurringListComponent implements OnInit {
  constructor(
    public readonly store: RecurringTransactionStore,
    private readonly router: Router,
    private readonly confirmDialog: ConfirmDialogService,
  ) {}

  ngOnInit(): void {
    this.store.load();
  }

  onEdit(item: RecurringTransactionResponse): void {
    this.router.navigate(['/recurring', item.id, 'edit']);
  }

  togglePause(item: RecurringTransactionResponse): void {
    if (item.paused) {
      this.store.resume(item.id).subscribe();
    } else {
      this.store.pause(item.id).subscribe();
    }
  }

  onDelete(item: RecurringTransactionResponse): void {
    this.confirmDialog
      .confirm({
        title: 'common.deleteTitle',
        message: 'common.deleteConfirm',
        messageParams: { name: item.description || item.categoryName },
        confirmLabel: 'common.delete',
        variant: 'danger',
      })
      .subscribe((confirmed) => {
        if (confirmed) this.store.delete(item.id).subscribe();
      });
  }

  frequencyKey(freq: string): string {
    return `recurring.frequencies.${freq.toLowerCase()}`;
  }
}
