import { CurrencyPipe, DOCUMENT } from '@angular/common';
import { Component, effect, Inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexLegend,
  ApexStroke,
  ApexTheme,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  NgApexchartsModule,
} from 'ng-apexcharts';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { TransactionService } from '../../../../core/services/transaction.service';
import { ThemeService } from '../../../../core/services/theme.service';
import { ChartCardComponent } from '../../../../shared/components/chart-card/chart-card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../../../shared/components/skeleton/skeleton.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import { StatCardSkeletonComponent } from '../../../../shared/components/stat-card/stat-card-skeleton.component';

const MONTHS = 6;

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  imports: [
    CurrencyPipe,
    TranslateModule,
    NgApexchartsModule,
    PageHeaderComponent,
    ChartCardComponent,
    StatCardComponent,
    StatCardSkeletonComponent,
    EmptyStateComponent,
    SkeletonComponent,
  ],
})
export class ReportsComponent implements OnInit {
  loading = true;
  error = false;
  hasData = false;

  totalIncome = 0;
  totalExpense = 0;
  balance = 0;

  // Income vs expense — last 6 months (area chart).
  trendSeries: ApexAxisChartSeries = [];
  trendXaxis: ApexXAxis = { categories: [] };
  readonly trendChart: ApexChart = {
    type: 'area',
    height: 320,
    fontFamily: 'Poppins, sans-serif',
    foreColor: '#8a8f98',
    toolbar: { show: false },
  };
  readonly trendStroke: ApexStroke = { curve: 'smooth', width: 2 };
  readonly trendDataLabels: ApexDataLabels = { enabled: false };
  // Income = --primary, expense = --muted-foreground; updated on theme change.
  readonly trendColors = signal<string[]>([]);
  readonly trendYaxis: ApexYAxis;
  readonly trendTooltip: ApexTooltip;

  // Expenses by category — current month (donut chart).
  categorySeries: number[] = [];
  categoryLabels: string[] = [];
  readonly donutChart: ApexChart = {
    type: 'donut',
    height: 320,
    fontFamily: 'Poppins, sans-serif',
    foreColor: '#8a8f98',
  };
  readonly donutLegend: ApexLegend = { position: 'bottom' };
  // Monochrome ramp of --primary instead of a rainbow palette; theme-reactive.
  readonly donutTheme = signal<ApexTheme>({});
  readonly donutTooltip: ApexTooltip;

  constructor(
    private readonly transactionService: TransactionService,
    private readonly translate: TranslateService,
    private readonly themeService: ThemeService,
    @Inject(LOCALE_ID) locale: string,
    @Inject(DOCUMENT) private readonly document: Document,
  ) {
    // Currency formatter for chart axes/tooltips — Angular pipes can't be used
    // inside ApexCharts callbacks, and bare numbers there would break the
    // BRL-formatted look used across the app.
    const currency = new Intl.NumberFormat(locale, { style: 'currency', currency: 'BRL' });
    const formatter = (value: number): string => currency.format(value);
    this.trendYaxis = { labels: { formatter } };
    this.trendTooltip = { y: { formatter } };
    this.donutTooltip = { y: { formatter } };

    // Re-derive chart colors from the theme palette on every theme change,
    // deferred so the read runs after ThemeService applies it to <html>.
    this.applyChartColors();
    effect(() => {
      this.themeService.theme();
      queueMicrotask(() => this.applyChartColors());
    });
  }

  private applyChartColors(): void {
    const root = this.document.documentElement;
    const styles = getComputedStyle(root);
    const primary = styles.getPropertyValue('--primary').trim() || '#2490FF';
    const muted = styles.getPropertyValue('--muted-foreground').trim() || '#64748B';
    const isDark = root.classList.contains('dark');
    this.trendColors.set([primary, muted]);
    this.donutTheme.set({
      monochrome: { enabled: true, color: primary, shadeTo: isDark ? 'light' : 'dark', shadeIntensity: 0.6 },
    });
  }

  ngOnInit(): void {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth() - (MONTHS - 1), 1);
    this.transactionService.list(ReportsComponent.isoDate(start), ReportsComponent.isoDate(now)).subscribe({
      next: (transactions) => {
        this.build(transactions);
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  private build(transactions: TransactionResponse[]): void {
    this.hasData = transactions.length > 0;
    if (!this.hasData) {
      return;
    }

    const now = new Date();
    const monthKeys: string[] = [];
    const monthLabels: string[] = [];
    for (let i = MONTHS - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      monthKeys.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
      monthLabels.push(d.toLocaleDateString(this.translate.currentLang || 'pt-BR', { month: 'short' }));
    }

    const income = new Array<number>(MONTHS).fill(0);
    const expense = new Array<number>(MONTHS).fill(0);
    const byCategory = new Map<string, number>();
    const currentMonth = monthKeys[MONTHS - 1];

    for (const t of transactions) {
      const month = t.date.slice(0, 7);
      const idx = monthKeys.indexOf(month);
      if (idx < 0) {
        continue;
      }
      if (t.type === 'INCOME') {
        income[idx] += t.amount;
      } else {
        expense[idx] += t.amount;
        if (month === currentMonth) {
          byCategory.set(t.categoryName, (byCategory.get(t.categoryName) ?? 0) + t.amount);
        }
      }
    }

    this.totalIncome = income.reduce((a, b) => a + b, 0);
    this.totalExpense = expense.reduce((a, b) => a + b, 0);
    this.balance = this.totalIncome - this.totalExpense;

    this.trendXaxis = { categories: monthLabels };
    this.trendSeries = [
      { name: this.translate.instant('reports.income'), data: income },
      { name: this.translate.instant('reports.expense'), data: expense },
    ];

    const sorted = [...byCategory.entries()].sort((a, b) => b[1] - a[1]);
    this.categoryLabels = sorted.map(([name]) => name);
    this.categorySeries = sorted.map(([, value]) => value);
  }

  private static isoDate(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
