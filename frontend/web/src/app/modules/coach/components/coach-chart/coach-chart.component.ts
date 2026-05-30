import { DOCUMENT } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject, Input, LOCALE_ID, effect, signal, OnInit } from '@angular/core';
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
import { CoachChart } from '../../../../core/models/coach.model';
import { ThemeService } from '../../../../core/services/theme.service';

/**
 * Renders a Coach-driven chart (line or donut) inside the assistant bubble.
 *
 * Colors are derived from the live CSS variables (--primary, --muted-foreground)
 * so the chart re-paints when the user switches between light/dark or picks a
 * different accent. This mirrors the pattern used by reports.component.ts.
 */
@Component({
  selector: 'app-coach-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgApexchartsModule],
  templateUrl: './coach-chart.component.html',
})
export class CoachChartComponent implements OnInit {
  @Input({ required: true }) chart!: CoachChart;

  // Line-chart bindings.
  readonly lineSeries = signal<ApexAxisChartSeries>([]);
  readonly lineXaxis = signal<ApexXAxis>({ categories: [] });
  readonly lineColors = signal<string[]>([]);
  readonly lineChart: ApexChart = {
    type: 'line',
    height: 260,
    fontFamily: 'Poppins, sans-serif',
    foreColor: '#8a8f98',
    toolbar: { show: false },
    parentHeightOffset: 0,
    sparkline: { enabled: false },
  };
  readonly lineStroke: ApexStroke = { curve: 'smooth', width: 2 };
  readonly lineLegend: ApexLegend = { position: 'top', horizontalAlign: 'right' };
  readonly lineDataLabels: ApexDataLabels = { enabled: false };
  readonly lineYaxis: ApexYAxis;
  readonly lineTooltip: ApexTooltip;

  // Donut bindings.
  readonly donutSeries = signal<number[]>([]);
  readonly donutLabels = signal<string[]>([]);
  readonly donutTheme = signal<ApexTheme>({});
  readonly donutChart: ApexChart = {
    type: 'donut',
    height: 280,
    fontFamily: 'Poppins, sans-serif',
    foreColor: '#8a8f98',
  };
  readonly donutLegend: ApexLegend = { position: 'bottom' };
  readonly donutTooltip: ApexTooltip;

  constructor(
    private readonly themeService: ThemeService,
    @Inject(LOCALE_ID) locale: string,
    @Inject(DOCUMENT) private readonly document: Document,
  ) {
    const currency = new Intl.NumberFormat(locale, { style: 'currency', currency: 'BRL' });
    const formatter = (value: number): string => currency.format(value);
    this.lineYaxis = { labels: { formatter } };
    this.lineTooltip = { y: { formatter } };
    this.donutTooltip = { y: { formatter } };

    this.applyTokens();
    effect(() => {
      this.themeService.theme();
      queueMicrotask(() => this.applyTokens());
    });
  }

  ngOnInit(): void {
    this.hydrate();
  }

  private hydrate(): void {
    if (this.chart.kind === 'line' && 'categories' in this.chart.data) {
      this.lineXaxis.set({ categories: [...this.chart.data.categories] });
      this.lineSeries.set(this.chart.data.series.map((s) => ({ name: s.name, data: [...s.data] })));
    } else if (this.chart.kind === 'donut' && 'labels' in this.chart.data) {
      this.donutLabels.set([...this.chart.data.labels]);
      this.donutSeries.set([...this.chart.data.series]);
    }
  }

  private applyTokens(): void {
    const root = this.document.documentElement;
    const styles = getComputedStyle(root);
    const primary = styles.getPropertyValue('--primary').trim() || '#2490FF';
    const muted = styles.getPropertyValue('--muted-foreground').trim() || '#64748B';
    const isDark = root.classList.contains('dark');
    this.lineColors.set([primary, muted]);
    this.donutTheme.set({
      monochrome: {
        enabled: true,
        color: primary,
        shadeTo: isDark ? 'light' : 'dark',
        shadeIntensity: 0.6,
      },
    });
  }
}
