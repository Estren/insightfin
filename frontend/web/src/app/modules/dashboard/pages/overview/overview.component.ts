import { Component, OnInit } from '@angular/core';
import { AsyncPipe, CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { DashboardStore } from '../../../../core/stores/dashboard.store';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  imports: [AsyncPipe, CurrencyPipe, DatePipe, NgClass],
})
export class OverviewComponent implements OnInit {
  constructor(public readonly dashboardStore: DashboardStore) {}

  ngOnInit(): void {
    const now = new Date();
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    this.dashboardStore.load(month);
  }
}
