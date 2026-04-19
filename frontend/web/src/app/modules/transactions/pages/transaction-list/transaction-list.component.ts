import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { map, Observable } from 'rxjs';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { TransactionStore } from '../../../../core/stores/transaction.store';

interface TransactionGroup {
  date: string;
  transactions: TransactionResponse[];
}

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  imports: [AsyncPipe, CurrencyPipe, DatePipe, NgClass, RouterLink],
})
export class TransactionListComponent implements OnInit {
  readonly groupedTransactions$: Observable<TransactionGroup[]>;

  constructor(
    public readonly transactionStore: TransactionStore,
    private readonly router: Router,
  ) {
    this.groupedTransactions$ = this.transactionStore.transactions$.pipe(map((list) => this.groupByDate(list)));
  }

  ngOnInit(): void {
    this.transactionStore.load(this.transactionStore.selectedMonth);
  }

  changeMonth(delta: number): void {
    const [year, month] = this.transactionStore.selectedMonth.split('-').map(Number);
    const next = new Date(year, month - 1 + delta, 1);
    const nextMonth = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`;
    this.transactionStore.load(nextMonth);
  }

  onEdit(transaction: TransactionResponse): void {
    this.router.navigate(['/transactions', transaction.id, 'edit']);
  }

  onDelete(transaction: TransactionResponse): void {
    const label = transaction.description || transaction.categoryName;
    const confirmed = window.confirm(`Delete transaction "${label}"? This cannot be undone.`);
    if (!confirmed) return;
    this.transactionStore.delete(transaction.id).subscribe();
  }

  private groupByDate(list: TransactionResponse[]): TransactionGroup[] {
    const map = new Map<string, TransactionResponse[]>();
    for (const t of list) {
      const bucket = map.get(t.date);
      if (bucket) {
        bucket.push(t);
      } else {
        map.set(t.date, [t]);
      }
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => (a < b ? 1 : -1))
      .map(([date, transactions]) => ({ date, transactions }));
  }
}
