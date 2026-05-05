import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { TransactionStore } from '../../../../core/stores/transaction.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { Tab, TabsComponent } from '../../../../shared/components/tabs/tabs.component';
import { TransactionCreateComponent } from '../transaction-create/transaction-create.component';
import { TransactionEditComponent } from '../transaction-edit/transaction-edit.component';

type TabValue = 'ALL' | 'INCOME' | 'EXPENSE';

interface TransactionGroup {
  date: string;
  transactions: TransactionResponse[];
}

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    DatePipe,
    NgClass,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingComponent,
    TabsComponent,
    DataTableComponent,
    ModalComponent,
    TransactionCreateComponent,
    TransactionEditComponent,
  ],
})
export class TransactionListComponent implements OnInit {
  readonly tabs: Tab[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Revenue', value: 'INCOME' },
    { label: 'Expenses', value: 'EXPENSE' },
  ];

  private readonly _activeTab$ = new BehaviorSubject<TabValue>('ALL');
  readonly activeTab$ = this._activeTab$.asObservable();
  readonly groupedTransactions$: Observable<TransactionGroup[]>;

  showCreate = false;
  editingId: string | null = null;

  constructor(public readonly transactionStore: TransactionStore) {
    this.groupedTransactions$ = combineLatest([this.transactionStore.transactions$, this._activeTab$]).pipe(
      map(([transactions, tab]) => {
        const filtered = tab === 'ALL' ? transactions : transactions.filter((t) => t.type === tab);
        return this.groupByDate(filtered);
      }),
    );
  }

  ngOnInit(): void {
    this.transactionStore.load(this.transactionStore.selectedMonth);
  }

  setTab(value: string): void {
    this._activeTab$.next(value as TabValue);
  }

  changeMonth(delta: number): void {
    const [year, month] = this.transactionStore.selectedMonth.split('-').map(Number);
    const next = new Date(year, month - 1 + delta, 1);
    const nextMonth = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`;
    this.transactionStore.load(nextMonth);
  }

  openCreate(): void {
    this.showCreate = true;
  }

  onEdit(transaction: TransactionResponse): void {
    this.editingId = transaction.id;
  }

  closeModals(): void {
    this.showCreate = false;
    this.editingId = null;
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
