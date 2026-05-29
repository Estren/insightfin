import { AsyncPipe, CurrencyPipe, DatePipe, DOCUMENT, NgClass } from '@angular/common';
import { Component, effect, Inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
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
import { AiFeedbackResponse } from '../../../../core/models/ai-feedback.model';
import { BudgetStatusResponse } from '../../../../core/models/budget.model';
import { DashboardResponse } from '../../../../core/models/dashboard.model';
import { extractLatestHealthScore, HealthScoreMetadata } from '../../../../core/models/health-score.model';
import { TransactionResponse } from '../../../../core/models/transaction.model';
import { ThemeService } from '../../../../core/services/theme.service';
import { TransactionService } from '../../../../core/services/transaction.service';
import { AiFeedbackStore } from '../../../../core/stores/ai-feedback.store';
import { DashboardStore } from '../../../../core/stores/dashboard.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { FeedbackCardComponent } from '../../../../shared/components/feedback-card/feedback-card.component';
import { FeedbackCardSkeletonComponent } from '../../../../shared/components/feedback-card/feedback-card-skeleton.component';
import { HealthScoreGaugeComponent } from '../../../../shared/components/health-score-gauge/health-score-gauge.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../../../shared/components/skeleton/skeleton.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';

/** A proactive Coach suggestion shown on the dashboard. The `message*` keys
 *  render the card text (reactive to language); the `question*` keys are
 *  resolved at click time into the prompt sent to the Coach. */
interface CoachPrompt {
  icon: string;
  messageKey: string;
  messageParams?: Record<string, string | number>;
  questionKey: string;
  questionParams?: Record<string, string | number>;
}

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  imports: [
    AsyncPipe,
    CurrencyPipe,
    DatePipe,
    NgClass,
    RouterLink,
    TranslateModule,
    AngularSvgIconModule,
    NgApexchartsModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    FeedbackCardComponent,
    FeedbackCardSkeletonComponent,
    HealthScoreGaugeComponent,
    SkeletonComponent,
    StatCardComponent,
  ],
})
export class OverviewComponent implements OnInit {
  chartsLoading = true;
  hasExpenses = false;

  // Cumulative spending across the days of the current month (area chart),
  // styled to match the charts on /reports.
  cumulativeSeries: ApexAxisChartSeries = [];
  cumulativeXaxis: ApexXAxis = { categories: [] };
  readonly cumulativeChart: ApexChart = {
    type: 'area',
    height: 320,
    fontFamily: 'Poppins, sans-serif',
    foreColor: '#8a8f98',
    toolbar: { show: false },
  };
  readonly cumulativeStroke: ApexStroke = { curve: 'smooth', width: 2 };
  readonly cumulativeDataLabels: ApexDataLabels = { enabled: false };
  // Derived from the theme's --primary and updated on theme change, so the
  // charts track the responsive palette instead of hard-coded colors.
  readonly cumulativeColors = signal<string[]>([]);
  readonly cumulativeYaxis: ApexYAxis;
  readonly cumulativeTooltip: ApexTooltip;

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
  // Monochrome ramp built from --primary, so the slices read as one color
  // family (theme-aware) rather than a rainbow.
  readonly donutTheme = signal<ApexTheme>({});
  readonly donutTooltip: ApexTooltip;

  criticalBudgetsCount(statuses: BudgetStatusResponse[]): number {
    return statuses.filter((b) => b.percentageUsed >= 90).length;
  }

  /** Picks the latest HEALTH_SCORE feedback from the current month, parsed and validated. */
  healthScore(feedbacks: AiFeedbackResponse[]): HealthScoreMetadata | null {
    return extractLatestHealthScore(feedbacks);
  }

  /** Up to two proactive Coach prompts derived from the loaded dashboard data:
   *  the worst over-limit budget, and a low health score. Empty when neither
   *  condition holds, so the section hides itself. */
  coachPrompts(summary: DashboardResponse, feedbacks: AiFeedbackResponse[]): CoachPrompt[] {
    const prompts: CoachPrompt[] = [];

    const over = summary.budgetStatuses
      .filter((b) => b.percentageUsed > 100)
      .sort((a, b) => b.percentageUsed - a.percentageUsed);
    if (over.length > 0) {
      const worst = over[0];
      prompts.push({
        icon: 'assets/icons/heroicons/outline/exclamation-triangle.svg',
        messageKey: 'dashboard.coachPrompts.budgetOver',
        messageParams: { category: worst.categoryName, percent: Math.round(worst.percentageUsed) },
        questionKey: 'dashboard.coachPrompts.budgetOverQuestion',
        questionParams: { category: worst.categoryName },
      });
    }

    const score = this.healthScore(feedbacks);
    if (score && score.score < 50) {
      prompts.push({
        icon: 'assets/icons/heroicons/outline/chat-bubble.svg',
        messageKey: 'dashboard.coachPrompts.lowScore',
        messageParams: { score: score.score },
        questionKey: 'dashboard.coachPrompts.lowScoreQuestion',
      });
    }

    return prompts.slice(0, 2);
  }

  askCoach(prompt: CoachPrompt): void {
    const question = this.translate.instant(prompt.questionKey, prompt.questionParams);
    this.router.navigate(['/coach'], { state: { question } });
  }

  constructor(
    public readonly dashboardStore: DashboardStore,
    public readonly feedbackStore: AiFeedbackStore,
    private readonly router: Router,
    private readonly translate: TranslateService,
    private readonly transactionService: TransactionService,
    private readonly themeService: ThemeService,
    @Inject(LOCALE_ID) locale: string,
    @Inject(DOCUMENT) private readonly document: Document,
  ) {
    // Currency formatter for chart axes/tooltips — Angular pipes can't be used
    // inside ApexCharts callbacks. Mirrors the formatter on /reports.
    const currency = new Intl.NumberFormat(locale, { style: 'currency', currency: 'BRL' });
    const formatter = (value: number): string => currency.format(value);
    this.cumulativeYaxis = { labels: { formatter } };
    this.cumulativeTooltip = { y: { formatter } };
    this.donutTooltip = { y: { formatter } };

    // Re-derive the chart colors from --primary whenever the theme changes so
    // they track the responsive palette live. The read is deferred to a
    // microtask so it runs after ThemeService has applied the theme to <html>.
    this.applyChartColors();
    effect(() => {
      this.themeService.theme();
      queueMicrotask(() => this.applyChartColors());
    });
  }

  private applyChartColors(): void {
    const root = this.document.documentElement;
    const primary = getComputedStyle(root).getPropertyValue('--primary').trim() || '#2490FF';
    const isDark = root.classList.contains('dark');
    this.cumulativeColors.set([primary]);
    this.donutTheme.set({
      monochrome: { enabled: true, color: primary, shadeTo: isDark ? 'light' : 'dark', shadeIntensity: 0.6 },
    });
  }

  ngOnInit(): void {
    const now = new Date();
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    this.dashboardStore.load(month);
    this.feedbackStore.load(month);
    this.loadCharts(now);
  }

  /** Fetches the current month's transactions and derives both charts from
   *  that single request (cumulative spending + expenses by category). */
  private loadCharts(now: Date): void {
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    this.transactionService.list(OverviewComponent.isoDate(start), OverviewComponent.isoDate(now)).subscribe({
      next: (transactions) => {
        this.buildCharts(transactions, now);
        this.chartsLoading = false;
      },
      error: () => {
        this.chartsLoading = false;
      },
    });
  }

  private buildCharts(transactions: TransactionResponse[], now: Date): void {
    const todayDay = now.getDate();
    const daily = new Array<number>(todayDay + 1).fill(0);
    const byCategory = new Map<string, number>();

    for (const t of transactions) {
      if (t.type !== 'EXPENSE') {
        continue;
      }
      // Cap at today so future-dated entries (e.g. recurring) don't distort the
      // "progress so far" line; the donut still counts the whole month to date.
      const day = parseInt(t.date.slice(8, 10), 10);
      if (day >= 1 && day <= todayDay) {
        daily[day] += t.amount;
      }
      byCategory.set(t.categoryName, (byCategory.get(t.categoryName) ?? 0) + t.amount);
    }

    const cumulative: number[] = [];
    const labels: string[] = [];
    let running = 0;
    for (let d = 1; d <= todayDay; d++) {
      running += daily[d];
      cumulative.push(running);
      labels.push(String(d));
    }

    this.hasExpenses = running > 0;
    this.cumulativeXaxis = { categories: labels };
    this.cumulativeSeries = [{ name: this.translate.instant('dashboard.charts.cumulativeSpending'), data: cumulative }];

    const sorted = [...byCategory.entries()].sort((a, b) => b[1] - a[1]);
    this.categoryLabels = sorted.map(([name]) => name);
    this.categorySeries = sorted.map(([, value]) => value);
  }

  private static isoDate(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
